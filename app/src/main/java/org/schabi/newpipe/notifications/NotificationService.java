package org.schabi.newpipe.notifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

public class NotificationService extends Service implements NewStreams.Callback {

	private NotificationHelper notificationHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		notificationHelper = new NotificationHelper(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new NewStreams(this, this).test();
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
		for (StreamInfoItem it : list) {
			notification.addItem(it.getName());
		}
		notificationHelper.post(notification);
	}
}
