package org.schabi.newpipe.notifications;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;
import org.schabi.newpipe.notifications.scheduler.ScheduleLogger;
import org.schabi.newpipe.notifications.scheduler.ScheduleOptions;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationService extends Service {

	private NotificationHelper notificationHelper;
	@Nullable
	private Disposable worker = null;
	private Flowable<ChannelUpdates> updatesFlowable;

	@Override
	public void onCreate() {
		super.onCreate();
		notificationHelper = new NotificationHelper(getApplicationContext());
		updatesFlowable = Flowable.create(new SubscriptionUpdates(getApplicationContext()), BackpressureStrategy.BUFFER)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (checkRequirements()) {
			if (worker != null) {
				worker.dispose();
			}
			worker = updatesFlowable.subscribe(notificationHelper::notify, this::onError, this::onComplete);
			new ScheduleLogger(this).log(this.getClass().getSimpleName() + " onStartCommand()").close();
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if (worker != null) {
			worker.dispose();
		}
		super.onDestroy();
	}

	private void onError(Throwable error) {
		new ScheduleLogger(getApplicationContext())
				.log(error.getClass().getSimpleName() + ": " + error.getMessage())
				.close();
		NotificationsScheduler.getInstance(this).reconfigure();
		stopSelf();
	}

	private void onComplete() {
		NotificationsScheduler.getInstance(this).reconfigure();
		stopSelf();
	}

	private boolean checkRequirements() {
		if (!NotificationsScheduler.isEnabled(getApplicationContext())) {
			return false;
		}
		final ScheduleOptions options = ScheduleOptions.from(getApplicationContext());
		if (options.isRequireNonMeteredNetwork()) {
			final ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = manager != null ? manager.getActiveNetworkInfo() : null;
			return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
		} else {
			return true;
		}
	}
}
