package org.schabi.newpipe.notifications.scheduler;

import android.content.SharedPreferences;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

public final class ScheduleOptions {

	private static final long INTERVAL_DEFAULT = TimeUnit.HOURS.toMillis(1);
	private static final long INTERVAL_THRESHOLD = TimeUnit.MINUTES.toMillis(1);

	private final long interval;
	private final boolean requireNonMeteredNetwork;

	public static ScheduleOptions from(SharedPreferences preferences) {
		return new ScheduleOptions(
				INTERVAL_DEFAULT,
				true
		);
	}

	public ScheduleOptions(long interval, boolean requireNonMeteredNetwork) {
		this.interval = interval;
		this.requireNonMeteredNetwork = requireNonMeteredNetwork;
	}

	@Deprecated
	public long getInterval() {
		return interval;
	}

	@Deprecated
	public long getMinInterval() {
		return (long) (interval * 0.9);
	}

	@Deprecated
	public long getMaxInterval() {
		return (long) (interval * 1.1);
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
}
