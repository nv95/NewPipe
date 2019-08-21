package org.schabi.newpipe.notifications;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import java.io.InputStream;

import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

final class NotificationIcon implements SingleOnSubscribe<Bitmap> {

	private final String url;
	private final int size;

	NotificationIcon(Context context, String url) {
		this.url = url;
		this.size = getIconSize(context);
	}

	@Override
	public void subscribe(SingleEmitter<Bitmap> singleEmitter) throws Exception {
		try {
			final OkHttpClient client = new OkHttpClient();
			final Request request = new Request.Builder()
					.url(url)
					.get()
					.build();
			final Response response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				final InputStream inputStream = response.body().byteStream();
				final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				singleEmitter.onSuccess(ThumbnailUtils.extractThumbnail(bitmap, size, size));
				bitmap.recycle();
			} else {
				singleEmitter.onError(new RuntimeException());
			}
		} catch (Exception e) {
			singleEmitter.onError(e);
		}
	}

	private static int getIconSize(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int size2 = activityManager != null ? activityManager.getLauncherLargeIconSize() : 0;
		int size1 = context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
		return size2 > size1 ? size2 : size1;
	}
}
