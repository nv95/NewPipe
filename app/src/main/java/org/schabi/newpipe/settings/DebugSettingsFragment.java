package org.schabi.newpipe.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.widget.ListView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.notifications.scheduler.ScheduleLogger;

import java.util.ArrayList;

public class DebugSettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.debug_settings);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (getString(R.string.show_notifications_log).equals(preference.getKey())) {
            showSchedulerLog();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    private void showSchedulerLog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setPositiveButton(R.string.close, null);
        final ArrayList<String> log = ScheduleLogger.getLogContent(requireContext().getApplicationContext());
        if (log == null || log.isEmpty()) {
            builder.setMessage(R.string.content_not_available);
            builder.show();
        } else {
            builder.setItems(log.toArray(new CharSequence[0]), null);
            builder.setNeutralButton(R.string.clear, (dialog, which) ->
                    ScheduleLogger.clear(requireContext().getApplicationContext()));
            final AlertDialog dialog = builder.create();
            final ListView listView = dialog.getListView();
            listView.setDivider(new ColorDrawable(Color.LTGRAY));
            listView.setDividerHeight(1);
            listView.setStackFromBottom(true);
            dialog.show();
        }
    }
}
