package org.schabi.newpipe.notifications.scheduler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;

import org.schabi.newpipe.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * TODO remove it after testing
 */
public final class ScheduleLogger implements Closeable {

	private static final int MAX_LINES = 40;

	@Nullable
	private final BufferedWriter writer;

	public ScheduleLogger(Context context) {
		BufferedWriter bufferedWriter = null;
		if (BuildConfig.DEBUG) {
			try {
				final File file = getLogFile(context);
				if (file != null) {
					bufferedWriter = new BufferedWriter(new FileWriter(file, true));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writer = bufferedWriter;
	}

	public ScheduleLogger log(String message) {
		if (writer == null) {
			return this;
		}
		try {
			final CharSequence dateTime = DateFormat.format("dd MMM kk:mm", System.currentTimeMillis());
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

	@Nullable
	private static File getLogFile(Context context) throws IOException {
		final File file = new File(context.getExternalFilesDir("logs"), "scheduler.log");
		return file.exists() || file.createNewFile() ? file : null;
	}

	@Nullable
	public static ArrayList<String> getLogContent(@NonNull Context context) {
		BufferedReader reader = null;
		try {
			final File file = getLogFile(context);
			if (file == null) {
				return null;
			}
			reader = new BufferedReader(new FileReader(file));
			final ArrayList<String> lines = new ArrayList<>();
			for (int i=0;i<MAX_LINES;i++) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}
			return lines;
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace();
			}
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void clear(@NonNull Context context) {
		try {
			final File file = getLogFile(context);
			if (file != null && file.exists()) {
				file.delete();
			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace();
			}
		}
	}
}
