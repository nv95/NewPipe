package org.schabi.newpipe.notifications;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.database.stream.dao.StreamDAO;
import org.schabi.newpipe.database.subscription.NotificationMode;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.local.subscription.SubscriptionService;
import org.schabi.newpipe.util.ExtractorHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewStreamsLoader implements Disposable {

	private final Context context;
	private final StreamDAO streamTable;
	private final Callback callback;
	@Nullable
	private Disposable loader = null;

	NewStreamsLoader(Context context, Callback callback) {
		this.context = context;
		this.callback = callback;
		this.streamTable = NewPipeDatabase.getInstance(context).streamDAO();
	}

	public void start() {
		loader = SubscriptionService.getInstance(context).getSubscription()
				.subscribeOn(Schedulers.newThread())
				.firstOrError()
				.flatMapObservable(Observable::fromIterable)
				.filter(subscription -> subscription.getNotificationMode() != NotificationMode.DISABLED)
				.flatMapSingle(subscription -> ExtractorHelper.getChannelInfo(subscription.getServiceId(), subscription.getUrl(), true))
				.map(channel -> ChannelUpdates.from(channel, filterStreams(channel.getRelatedItems())))
				.filter(ChannelUpdates::isNotEmpty)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(callback::onNewStreams, this::onError, this::onComplete);
	}

	@WorkerThread
	private List<StreamInfoItem> filterStreams(List<?> list) {
		final ArrayList<StreamInfoItem> streams = new ArrayList<>(list.size());
		for (Object o : list) {
			if (o instanceof StreamInfoItem) {
				final StreamInfoItem item = (StreamInfoItem) o;
				if (streamTable.exists(item.getServiceId(), item.getUrl())) {
					break;
				}
				streams.add(item);
			}
		}
		streams.trimToSize();
		return streams;
	}

	private void onError(Throwable e) {
		if (BuildConfig.DEBUG) {
			e.printStackTrace();
		}
		callback.onFinish(false);
	}

	private void onComplete() {
		callback.onFinish(true);
	}

	@Override
	public void dispose() {
		if (loader != null) {
			loader.dispose();
			loader = null;
		}
	}

	@Override
	public boolean isDisposed() {
		return loader == null || loader.isDisposed();
	}

	interface Callback {

		void onNewStreams(ChannelUpdates updates);

		void onFinish(boolean isSuccess);
	}
}
