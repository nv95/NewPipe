package org.schabi.newpipe.notifications.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.schabi.newpipe.notifications.StreamsCheckReceiver;

public final class AlarmNotificationScheduler extends NotificationsScheduler {

	private final AlarmManager alarmManager;

	AlarmNotificationScheduler(Context context) {
		super(context);
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	protected void setup(ScheduleOptions options) {
		alarmManager.set(AlarmManager.RTC_WAKEUP, options.getNextJobTime(), getIntent());
	}

	@Override
	protected void dismiss() {
		alarmManager.cancel(getIntent());
	}

	private PendingIntent getIntent() {
		Intent intent = new Intent(StreamsCheckReceiver.BROADCAST_ACTION);
		intent.addCategory(StreamsCheckReceiver.CATEGORY);
		return PendingIntent.getBroadcast(context, JOB_ID, intent, 0);
	}
}
