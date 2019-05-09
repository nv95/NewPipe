package org.schabi.newpipe.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;
import org.schabi.newpipe.notifications.scheduler.ScheduleLogger;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.List;

public class NotificationService extends Service implements NewStreams.Callback {

	private NotificationHelper notificationHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		if (!NotificationsScheduler.isEnabled(getApplicationContext())) {
			stopSelf();
			return;
		}
		notificationHelper = new NotificationHelper(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new NewStreams(getApplicationContext(), this).test();
		new ScheduleLogger(this).log(this.getClass().getSimpleName() + " onStartCommand()").close();
		return super.onStartCommand(intent, flags, startId);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onNewStreams(SubscriptionEntity subscription, List<StreamInfoItem> list) {
		final NotificationData notification = new NotificationData();
		notification.setTitle(subscription.getName());
		notification.setId((int) subscription.getUid());
		notification.setIconUrl(subscription.getAvatarUrl());
		notification.setIntent(NavigationHelper.getChannelIntent(
				this,
				subscription.getServiceId(),
				subscription.getUrl(),
				subscription.getName()
		));
		for (StreamInfoItem it : list) {
			notification.addItem(it.getName());
		}
		notificationHelper.post(notification);
	}

	@Override
	public void onFinish(boolean isSuccess) {
		NotificationsScheduler.getInstance(this).reconfigure();
		stopSelf();
	}
}
