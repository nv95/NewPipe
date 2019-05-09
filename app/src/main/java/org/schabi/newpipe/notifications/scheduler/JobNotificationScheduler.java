package org.schabi.newpipe.notifications.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.schabi.newpipe.notifications.NotificationJobService;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class JobNotificationScheduler extends NotificationsScheduler {

	private final JobScheduler jobScheduler;

	JobNotificationScheduler(Context context) {
		super(context);
		jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	protected void setup(ScheduleOptions options) {
		ComponentName serviceComponent = new ComponentName(context, NotificationJobService.class);
		JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
		if (options.isRequireNonMeteredNetwork()) {
			builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
		} else {
			builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
		}
		builder.setMinimumLatency(options.getMinimumLatency());
		builder.setOverrideDeadline(options.getDeadline());
		builder.setRequiresDeviceIdle(false);
		builder.setRequiresCharging(false);
		builder.setPersisted(false); //we do it manually
		jobScheduler.schedule(builder.build());
	}

	@Override
	protected void dismiss() {
		jobScheduler.cancel(JOB_ID);
	}
}
