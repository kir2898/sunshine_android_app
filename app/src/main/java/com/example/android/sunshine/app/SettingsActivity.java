package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //add preferences to the class
        addPreferencesFromResource(R.xml.pref_general);
        //For all preferences, attach an OnPreferenceChangeListener so that the UI summary can be
        //updated when the preference changes
        Preference locationPref = (Preference)findPreference(getString(R.string.pref_location_key));
        /*locationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference p){
              return true;
            }
        });*/
        bindPreferenceSummaryToValue(locationPref);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        if(preference instanceof ListPreference){
            //for list preferences, look up the correct display value in
            //the preference's entries list (since they ahve separate labels/values)
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if(prefIndex>=0){
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }else{
            //for other preferences, set the summary to the value's simple string representation
            preference.setSummary(stringValue);
        }
        return true;
    }
}
