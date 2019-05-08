package org.schabi.newpipe.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class StreamsCheckReceiver extends BroadcastReceiver {

	public static final String BROADCAST_ACTION = "org.schabi.newpipe.ACTION_CHECKSTREAMS";
	public static final String CATEGORY = "org.schabi.newpipe.CATEGORY_CHECKSTREAMS";

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent != null ? intent.getAction() : null;
		if (action == null) {
			return;
		}
		switch (action) {
			case BROADCAST_ACTION:
				context.startService(new Intent(context, NotificationService.class));
				break;
			case Intent.ACTION_BOOT_COMPLETED:
				new StreamsCheckScheduler(context).reconfigure();
				break;
		}
	}
}
