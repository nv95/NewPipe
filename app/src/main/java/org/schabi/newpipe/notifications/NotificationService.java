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

public class NotificationService extends Service implements NewStreamsLoader.Callback {

	private NotificationHelper notificationHelper;
	private NewStreamsLoader loader;

	@Override
	public void onCreate() {
		super.onCreate();
		notificationHelper = new NotificationHelper(getApplicationContext());
		loader = new NewStreamsLoader(getApplicationContext(), this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (checkRequirements()) {
			loader.start();
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
	public void onNewStreams(ChannelUpdates updates) {
		notificationHelper.notify(updates);
	}

	@Override
	public void onDestroy() {
		loader.dispose();
		super.onDestroy();
	}

	@Override
	public void onFinish(boolean isSuccess) {
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
