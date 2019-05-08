package org.schabi.newpipe.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.R;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public final class NotificationHelper {

	private final Context context;
	private final NotificationManager manager;
	private final CompositeDisposable disposable;

	public NotificationHelper(Context context) {
		this.context = context;
		this.disposable = new CompositeDisposable();
		this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void post(NotificationData data) {
		final String summary = context.getResources().getQuantityString(R.plurals.new_streams, data.getCount(), data.getCount());
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
				context.getString(R.string.streams_notification_channel_id));
		builder.setContentTitle(String.format("%s • %s", data.getTitle(),summary));
		builder.setContentText(data.getText());
		builder.setNumber(data.getCount());
		builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		builder.setSmallIcon(R.drawable.ic_stat_newpipe);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_newpipe_triangle_white));
		builder.setColor(ContextCompat.getColor(context, R.color.ic_launcher_background));
		builder.setColorized(true);
		builder.setAutoCancel(true);
		builder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
		final NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		for (CharSequence o : data.getItems()) {
			style.addLine(o);
		}
		style.setSummaryText(summary);
		style.setBigContentTitle(data.getTitle());
		builder.setStyle(style);
		builder.setContentIntent(PendingIntent.getActivity(
				context,
				data.getId(),
				data.getIntent(),
				0
		));

		disposable.add(
				Single.create(new NotificationIcon(context, data.getIconUrl()))
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.doAfterTerminate(() -> manager.notify(data.getId(), builder.build()))
						.subscribe(builder::setLargeIcon, throwable -> {
							if (BuildConfig.DEBUG) throwable.printStackTrace();
						})
		);
	}
}
