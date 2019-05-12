package org.schabi.newpipe.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.R;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

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

	public Context getContext() {
		return context;
	}

	public void notify(ChannelUpdates data) {
		final String summary = context.getResources().getQuantityString(R.plurals.new_streams, data.getCount(), data.getCount());
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
				context.getString(R.string.streams_notification_channel_id));
		builder.setContentTitle(String.format("%s • %s", data.getName(), summary));
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
		for (StreamInfoItem o : data.getStreams()) {
			style.addLine(o.getName());
		}
		style.setSummaryText(summary);
		style.setBigContentTitle(data.getName());
		builder.setStyle(style);
		builder.setContentIntent(PendingIntent.getActivity(
				context,
				data.getId(),
				data.createOpenChannelIntent(context),
				0
		));

		disposable.add(
				Single.create(new NotificationIcon(context, data.getAvatarUrl()))
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.doAfterTerminate(() -> manager.notify(data.getId(), builder.build()))
						.subscribe(builder::setLargeIcon, throwable -> {
							if (BuildConfig.DEBUG) throwable.printStackTrace();
						})
		);
	}

	/**
	 * Check whether notifications are not disabled by user via system settings
	 * @param context Context
	 * @return true if notifications are allowed, false otherwise
	 */
	public static boolean isNotificationsEnabledNative(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			final String channelId = context.getString(R.string.streams_notification_channel_id);
			final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (manager != null) {
				final NotificationChannel channel = manager.getNotificationChannel(channelId);
				return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
			} else {
				return false;
			}
		} else {
			return NotificationManagerCompat.from(context).areNotificationsEnabled();
		}
	}

	public static void openNativeSettingsScreen(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			final String channelId = context.getString(R.string.streams_notification_channel_id);
			final Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
					.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
					.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
			context.startActivity(intent);
		} else {
			final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + context.getPackageName()));
			context.startActivity(intent);
		}
	}
}
