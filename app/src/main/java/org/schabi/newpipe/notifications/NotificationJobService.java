package org.schabi.newpipe.notifications;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;
import org.schabi.newpipe.notifications.scheduler.ScheduleLogger;
import org.schabi.newpipe.notifications.scheduler.ScheduleOptions;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class NotificationJobService extends JobService {

	@Nullable
	private Job currentJob = null;

	@Override
	public boolean onStartJob(JobParameters params) {
		if (NotificationsScheduler.isEnabled(getApplicationContext())) {
			currentJob = new Job(this, ScheduleOptions
					.from(getApplicationContext()), params);
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

	class Job implements Disposable {

		private final ScheduleOptions options;
		private final JobParameters parameters;
		@Nullable
		private Disposable worker = null;
		private final Flowable<ChannelUpdates> updatesFlowable;
		private final NotificationHelper notificationHelper;

		Job(Context context, ScheduleOptions options, JobParameters parameters) {
			this.options = options;
			this.parameters = parameters;
			this.updatesFlowable = Flowable.create(new SubscriptionUpdates(context), BackpressureStrategy.BUFFER)
					.subscribeOn(Schedulers.newThread())
					.observeOn(AndroidSchedulers.mainThread());
			this.notificationHelper = new NotificationHelper(context);
		}

		void start() {
			worker = updatesFlowable.subscribe(notificationHelper::notify, this::onError, this::onComplete);
		}

		@Override
		public void dispose() {
			if (worker != null) {
				worker.dispose();
			}
		}

		@Override
		public boolean isDisposed() {
			return worker == null || worker.isDisposed();
		}

		private void onError(Throwable error) {
			new ScheduleLogger(getApplicationContext())
					.log(error.getClass().getSimpleName() + ": " + error.getMessage())
					.close();
			jobFinished(parameters, true);
		}

		private void onComplete() {
			NotificationsScheduler.getInstance(getApplicationContext()).reconfigure();
			jobFinished(parameters, false);
		}
	}
}
