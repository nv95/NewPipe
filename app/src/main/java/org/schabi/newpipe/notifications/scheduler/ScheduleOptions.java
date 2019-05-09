package org.schabi.newpipe.notifications.scheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import org.schabi.newpipe.R;

import java.util.concurrent.TimeUnit;

public final class ScheduleOptions {

	private static final String WIFI = "wifi";
	private static final long INTERVAL_THRESHOLD = TimeUnit.MINUTES.toMillis(1);

	private final long interval;
	private final boolean requireNonMeteredNetwork;

	public static ScheduleOptions from(Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return new ScheduleOptions(
				TimeUnit.HOURS.toMillis(Long.parseLong(preferences.getString(context.getString(R.string.streams_notifications_interval_key),
						context.getString(R.string.streams_notifications_interval_default)))),
				WIFI.equals(preferences.getString(context.getString(R.string.streams_notifications_network_key),
						context.getString(R.string.streams_notifications_network_default)))
		);
	}

	public ScheduleOptions(long interval, boolean requireNonMeteredNetwork) {
		this.interval = interval;
		this.requireNonMeteredNetwork = requireNonMeteredNetwork;
	}

	public boolean isRequireNonMeteredNetwork() {
		return requireNonMeteredNetwork;
	}

	public long getNextJobTime() {
		final long now = System.currentTimeMillis();
		final long elapsed = SystemClock.elapsedRealtime();

		final long nextTime = elapsed - (elapsed % interval) + interval + (now - elapsed);
		return nextTime < now + INTERVAL_THRESHOLD ? nextTime + interval : nextTime;
	}

	public long getMinimumLatency() {
		return getNextJobTime() - System.currentTimeMillis();
	}

	public long getDeadline() {
		return getMinimumLatency() + INTERVAL_THRESHOLD;
	}

	public long getRetryDelay() {
		return TimeUnit.MINUTES.toMillis(10);
	}
}
