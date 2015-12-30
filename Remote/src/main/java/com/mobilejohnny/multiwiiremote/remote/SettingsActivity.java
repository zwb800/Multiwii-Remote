package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.view.View;
import android.widget.Button;

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
public class SettingsActivity extends BaseSettingsActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            Button btnStart = new Button(this);
            btnStart.setText("Start");
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent("com.mobilejohnny.multiwiiremote.remote.action.REMOTE_NATIVE"));
                }
            });

//            setListFooter(btnStart);

            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
            return;
        }

        addPreferencesFromResource(R.xml.pref_main);
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle("Connection");
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_connect);

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle("Adjust");
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_adjust);

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle("JoyStick");
        getPreferenceScreen().addPreference(fakeHeader);

        Preference preference = new Preference(this);
        preference.setTitle("Joystick");
        preference.setIntent(new Intent("com.mobilejohnny.multiwiiremote.remote.action.REMOTE_NATIVE"));
        getPreferenceScreen().addPreference(preference);

        final Preference prefConnection =  findPreference("connection_type");
        final Preference prefHost =  findPreference("host");
        final Preference prefPort =  findPreference("port");
        final Preference prefDeviceName =  findPreference("device_name");
        final Preference prefMiddlePitch =  findPreference("middle_pitch");
        final Preference prefMiddleRoll =  findPreference("middle_roll");

        bindPreference(prefConnection, prefHost, prefPort, prefDeviceName, prefMiddlePitch, prefMiddleRoll);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_connect);
            addPreferencesFromResource(R.xml.pref_adjust);
            addPreferencesFromResource(R.xml.pref_headers);

            final Preference prefConnection =  findPreference("connection_type");
            final Preference prefHost =  findPreference("host");
            final Preference prefPort =  findPreference("port");
            final Preference prefDeviceName =  findPreference("device_name");
            final Preference prefMiddlePitch =  findPreference("middle_pitch");
            final Preference prefMiddleRoll =  findPreference("middle_roll");

            bindPreference(prefConnection, prefHost, prefPort, prefDeviceName, prefMiddlePitch, prefMiddleRoll);
        }
    }

    protected static void bindPreference(Preference prefConnection, final Preference prefHost, final Preference prefPort, final Preference prefDeviceName, Preference prefMiddlePitch, Preference prefMiddleRoll) {
        bindPreferenceSummaryToValue(prefDeviceName);
        bindPreferenceSummaryToValue(prefHost);
        bindPreferenceSummaryToValue(prefPort);

        bindPreferenceSummaryToValue(prefMiddlePitch);
        bindPreferenceSummaryToValue(prefMiddleRoll);

        final Context context = prefConnection.getContext();
        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                String[] connectType = context.getResources().getStringArray(R.array.pref_connect_list_values);
                String val = o.toString();
                boolean isbluetooth = val.equals(connectType[0]);
                boolean istcpudp =val.equals(connectType[1])||val.equals(connectType[2]);
                prefDeviceName.setEnabled(isbluetooth);

                prefHost.setEnabled(istcpudp);
                prefPort.setEnabled(istcpudp);

                return sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,o);
            }
        };

        prefConnection.setOnPreferenceChangeListener(onPreferenceChangeListener);
        onPreferenceChangeListener.onPreferenceChange(prefConnection, PreferenceManager
                .getDefaultSharedPreferences(prefConnection.getContext())
                .getString(prefConnection.getKey(), ""));
    }
}
