package com.mobilejohnny.multiwiiremote.remote;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }  else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);
            addPreferencesFromResource(R.xml.pref_adjust);

            final Preference prefConnection =  findPreference("connection_type");
            final Preference prefHost =  findPreference("host");
            final Preference prefPort =  findPreference("port");
            final Preference prefDeviceName =  findPreference("device_name");
            final Preference prefMiddlePitch =  findPreference("middle_pitch");
            final Preference prefMiddleRoll =  findPreference("middle_roll");


            bindPreferenceSummaryToValue(prefDeviceName);
            bindPreferenceSummaryToValue(prefHost);
            bindPreferenceSummaryToValue(prefPort);
            bindPreferenceSummaryToValue(prefMiddlePitch);
            bindPreferenceSummaryToValue(prefMiddleRoll);

            Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    String[] connectType = getResources().getStringArray(R.array.pref_connect_list_values);
                    boolean isbluetooth = o.toString().equals(connectType[0]);
                    prefDeviceName.setEnabled(isbluetooth);
                    prefHost.setEnabled(!isbluetooth);
                    prefPort.setEnabled(!isbluetooth);

                    return sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,o);
                }
            };

            prefConnection.setOnPreferenceChangeListener(onPreferenceChangeListener);
            onPreferenceChangeListener.onPreferenceChange(prefConnection,PreferenceManager
                    .getDefaultSharedPreferences(prefConnection.getContext())
                    .getString(prefConnection.getKey(), ""));
        }
    }



}
