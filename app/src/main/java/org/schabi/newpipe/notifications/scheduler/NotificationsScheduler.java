package org.schabi.newpipe.notifications.scheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import org.schabi.newpipe.R;

public abstract class NotificationsScheduler {

	static final int JOB_ID = 346501;

	public static boolean isEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getString(R.string.enable_streams_notifications), false);
	}

	public static NotificationsScheduler getInstance(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return new JobNotificationScheduler(context);
		} else {
			return new AlarmNotificationScheduler(context);
		}
	}

	protected final Context context;

	NotificationsScheduler(Context context) {
		this.context = context;
	}

	public void reconfigure() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		dismiss();
		final boolean enabled = preferences.getBoolean(context.getString(R.string.enable_streams_notifications), false);
		if (enabled) {
			final ScheduleOptions options = ScheduleOptions.from(context);
			setup(options);
			new ScheduleLogger(context).log(this.getClass().getSimpleName() + " reconfigure(),  expected at "
					+ DateFormat.format("dd MMM kk:mm", options.getNextJobTime())).close();
		}
	}

	protected abstract void setup(ScheduleOptions options);

	protected abstract void dismiss();
}
