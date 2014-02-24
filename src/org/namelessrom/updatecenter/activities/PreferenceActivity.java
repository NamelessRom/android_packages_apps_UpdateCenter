package org.namelessrom.updatecenter.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

import java.util.List;

/**
 * Created by alex on 17.02.14.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Some action");
            setListFooter(button);
        }*/
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class PrefsUpdates extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        private SharedPreferences mPrefs;
        private ListPreference mUpdateCheck;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_update);

            mUpdateCheck = (ListPreference) findPreference(Constants.UPDATE_CHECK_PREF);

            // Load the stored preference data
            mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (mUpdateCheck != null) {
                int check = mPrefs.getInt(Constants.UPDATE_CHECK_PREF, Constants.UPDATE_FREQ_WEEKLY);
                mUpdateCheck.setValue(String.valueOf(check));
                mUpdateCheck.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mUpdateCheck) {
                int value = Integer.valueOf((String) newValue);
                mPrefs.edit().putInt(Constants.UPDATE_CHECK_PREF, value).apply();
                Helper.scheduleUpdateService(getActivity(), value * 1000);
                return true;
            }

            return false;
        }

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PrefsUpdates.class.getName().equals(fragmentName)
                || super.isValidFragment(fragmentName);
    }
}
