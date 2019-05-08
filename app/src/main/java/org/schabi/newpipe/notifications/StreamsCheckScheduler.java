package org.schabi.newpipe.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.schabi.newpipe.R;

import java.util.concurrent.TimeUnit;

public final class StreamsCheckScheduler {

	private final Context context;
	private final long interval;

	public StreamsCheckScheduler(Context context) {
		this.context = context;
		interval = TimeUnit.HOURS.toMillis(3);
	}

	private void setupAlarmManager(boolean active) {
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if (alarmManager == null) return;
		Intent intent = new Intent(StreamsCheckReceiver.BROADCAST_ACTION);
		intent.addCategory(StreamsCheckReceiver.CATEGORY);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		if (active) {
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, alarmIntent);
		} else {
			alarmManager.cancel(alarmIntent);
		}
	}

	public void reconfigure() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		final boolean enabled = preferences.getBoolean(context.getString(R.string.enable_streams_notifications), false);
		setupAlarmManager(enabled);
	}
}
