package org.schabi.newpipe.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;

import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.subscription.NotificationMode;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.local.subscription.SubscriptionService;
import org.schabi.newpipe.notifications.NotificationHelper;
import org.schabi.newpipe.notifications.scheduler.NotificationsScheduler;

import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class NotificationsSettingsFragment extends BasePreferenceFragment {

	@Nullable
	private NotificationsScheduler scheduler = null;
	@Nullable
	private Snackbar notificationWarningSnackbar = null;
	@Nullable
	private Disposable loader;

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

	@Override
	public void onResume() {
		super.onResume();
		final boolean enabled = NotificationHelper.isNotificationsEnabledNative(getContext());
		getPreferenceScreen().setEnabled(enabled);
		if (!enabled) {
			if (notificationWarningSnackbar == null) {
				notificationWarningSnackbar = Snackbar.make(getListView(), R.string.notifications_disabled, Snackbar.LENGTH_INDEFINITE);
				notificationWarningSnackbar.setAction(R.string.settings, v -> NotificationHelper.openNativeSettingsScreen(v.getContext()));
				notificationWarningSnackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.snackbar_action_color));
				notificationWarningSnackbar.addCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar transientBottomBar, int event) {
						super.onDismissed(transientBottomBar, event);
						notificationWarningSnackbar = null;
					}
				});
				notificationWarningSnackbar.show();
			}
		} else {
			if (notificationWarningSnackbar != null) {
				notificationWarningSnackbar.dismiss();
				notificationWarningSnackbar = null;
			}
		}
		if (loader != null) {
			loader.dispose();
		}
		loader = SubscriptionService.getInstance(requireContext())
				.getSubscription()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::updateSubscriptions, this::onError);
	}

	@Override
	public void onPause() {
		if (loader != null) {
			loader.dispose();
		}
		loader = null;
		super.onPause();
	}

	private void updateSubscriptions(List<SubscriptionEntity> list) {
		int notified = 0;
		for (SubscriptionEntity o : list) {
			if (o.getNotificationMode() != NotificationMode.DISABLED) {
				notified++;
			}
		}
		final Preference preference = findPreference(getString(R.string.streams_notifications_channels_key));
		if (preference != null) {
			preference.setSummary(String.format(Locale.US, "%d/%d", notified, list.size()));
		}
	}

	private void onError(Throwable e) {
		if (BuildConfig.DEBUG) {
			e.printStackTrace();
		}
	}
}
