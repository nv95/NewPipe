package org.schabi.newpipe.notifications;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;
import org.schabi.newpipe.notifications.scheduler.ScheduleLogger;
import org.schabi.newpipe.notifications.scheduler.ScheduleOptions;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.List;

import io.reactivex.disposables.Disposable;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class NotificationJobService extends JobService {

	@Nullable
	private Job currentJob = null;

	@Override
	public boolean onStartJob(JobParameters params) {
		if (NotificationsScheduler.isEnabled(getApplicationContext())) {
			currentJob = new Job(this, ScheduleOptions
					.from(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())), params);
			new ScheduleLogger(this).log(this.getClass().getSimpleName() + " onStartJob()").close();
			currentJob.start();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		if (currentJob != null) {
			if (currentJob.isDisposed()) {
				return false;
			} else {
				currentJob.dispose();
				return true;
			}
		} else {
			return false;
		}
	}

	class Job implements NewStreams.Callback, Disposable {

		private final ScheduleOptions options;
		private final JobParameters parameters;
		private final NewStreams streams;
		private final NotificationHelper notificationHelper;

		Job(Context context, ScheduleOptions options, JobParameters parameters) {
			this.options = options;
			this.parameters = parameters;
			this.streams = new NewStreams(context, this);
			this.notificationHelper = new NotificationHelper(context);
		}

		void start() {
			streams.test();
		}

		@Override
		public void dispose() {
			streams.dispose();
		}

		@Override
		public boolean isDisposed() {
			return streams.isDisposed();
		}

		@Override
		public void onNewStreams(SubscriptionEntity subscription, List<StreamInfoItem> list) {
			final NotificationData notification = new NotificationData();
			notification.setTitle(subscription.getName());
			notification.setId((int) subscription.getUid());
			notification.setIconUrl(subscription.getAvatarUrl());
			notification.setIntent(NavigationHelper.getChannelIntent(
					getApplicationContext(),
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
			if (isSuccess) {
				NotificationsScheduler.getInstance(getApplicationContext()).reconfigure();
				jobFinished(parameters, false);
			} else {
				jobFinished(parameters, true);
			}
		}
	}
}
