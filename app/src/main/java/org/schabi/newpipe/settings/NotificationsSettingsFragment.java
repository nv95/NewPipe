package org.schabi.newpipe.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;

import org.schabi.newpipe.R;
import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;

public class NotificationsSettingsFragment extends BasePreferenceFragment {

	private NotificationsScheduler scheduler = null;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.notifications_settings);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		if (activity != null) {
			scheduler = NotificationsScheduler.getInstance(activity);
		}
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if (scheduler != null) {
			scheduler.reconfigure();
		}
		return super.onPreferenceTreeClick(preference);
	}
}
