package org.schabi.newpipe.notifications;

import android.content.Context;
import android.support.annotation.WorkerThread;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.database.AppDatabase;
import org.schabi.newpipe.database.stream.dao.StreamDAO;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.local.subscription.SubscriptionService;
import org.schabi.newpipe.util.ExtractorHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewStreams implements Disposable {

	private final Context context;
	private final CompositeDisposable disposables;
	private final AppDatabase database;
	private final StreamDAO streamTable;
	private final Callback callback;

	NewStreams(Context context, Callback callback) {
		this.context = context;
		this.callback = callback;
		this.disposables = new CompositeDisposable();
		this.database = NewPipeDatabase.getInstance(context);
		this.streamTable = database.streamDAO();
	}

	public void test() {
		disposables.add(
				SubscriptionService.getInstance(context).getSubscription()
						.toObservable()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::processSubscriptions, this::onError, this::onComplete)
		);

	}

	private void processSubscriptions(List<SubscriptionEntity> subscriptions) {
		for (SubscriptionEntity entity : subscriptions) {
			disposables.add(
					ExtractorHelper.getChannelInfo(entity.getServiceId(), entity.getUrl(), true)
							.subscribeOn(Schedulers.io())
							.flatMap(channel ->
									ExtractorHelper.getMoreChannelItems(
											entity.getServiceId(),
											entity.getUrl(),
											channel.getNextPageUrl()
									)
							).map(infoItemsPage -> filterStreams(infoItemsPage.getItems()))
							.filter(list -> !list.isEmpty())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(list -> callback.onNewStreams(entity, list), this::onError)
			);
		}
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
		dispose();
		callback.onFinish(false);
	}

	private void onComplete() {
		dispose();
		callback.onFinish(true);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}

	@Override
	public boolean isDisposed() {
		return disposables.isDisposed();
	}

	interface Callback {

		void onNewStreams(SubscriptionEntity subscription, List<StreamInfoItem> list);

		void onFinish(boolean isSuccess);
	}
}
