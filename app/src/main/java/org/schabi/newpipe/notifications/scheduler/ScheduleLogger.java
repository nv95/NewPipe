package org.schabi.newpipe.notifications.scheduler;

import android.content.Context;
import android.support.annotation.Nullable;

import org.schabi.newpipe.BuildConfig;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO remove it after testing
 */
public final class ScheduleLogger implements Closeable {

	@Nullable
	private final BufferedWriter writer;

	public ScheduleLogger(Context context) {
		BufferedWriter bufferedWriter = null;
		if (BuildConfig.DEBUG) {
			try {
				final File file = new File(context.getExternalFilesDir("logs"), "scheduler.log");
				if (!file.exists()) file.createNewFile();
				bufferedWriter = new BufferedWriter(new FileWriter(file, true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writer = bufferedWriter;
	}

	public ScheduleLogger log(String message) {
		if (writer == null) return this;
		try {
			final String dateTime = SimpleDateFormat.getDateTimeInstance().format(new Date());
			writer.write(dateTime + ": \t" + message + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public void close() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ignored) {
			}
		}
	}
}
