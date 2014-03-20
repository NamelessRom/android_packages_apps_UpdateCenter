package org.namelessrom.updatecenter.fragments.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

/**
 * Created by alex on 20.03.14.
 */
public class UpdatePreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private SharedPreferences mPrefs;
    private ListPreference    mUpdateCheck;
    private ListPreference    mRecoveryType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_update);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mUpdateCheck = (ListPreference) findPreference(Constants.UPDATE_CHECK_PREF);
        if (mUpdateCheck != null) {
            final int check = mPrefs.getInt(Constants.UPDATE_CHECK_PREF,
                    Constants.UPDATE_FREQ_WEEKLY);
            mUpdateCheck.setValue(String.valueOf(check));
            mUpdateCheck.setOnPreferenceChangeListener(this);
        }

        mRecoveryType = (ListPreference) findPreference(Constants.PREF_RECOVERY_TYPE);
        if (mRecoveryType != null) {
            final int type = mPrefs.getInt(Constants.PREF_RECOVERY_TYPE,
                    Constants.RECOVERY_TYPE_BOTH);
            mRecoveryType.setValue(String.valueOf(type));
            mRecoveryType.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUpdateCheck) {
            final int value = Integer.valueOf((String) newValue);
            mPrefs.edit().putInt(Constants.UPDATE_CHECK_PREF, value).apply();
            Helper.scheduleUpdateService(getActivity(), value * 1000);
            return true;
        } else if (preference == mRecoveryType) {
            final int value = Integer.valueOf((String) newValue);
            mPrefs.edit().putInt(Constants.PREF_RECOVERY_TYPE, value).apply();
            return true;
        }

        return false;
    }

}
