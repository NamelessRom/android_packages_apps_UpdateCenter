package org.namelessrom.updatecenter.fragments.updates;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.widgets.AttachPreferenceFragment;

public class UpdatePreferenceFragment extends AttachPreferenceFragment implements Constants,
        Preference.OnPreferenceChangeListener {

    private SharedPreferences mPrefs;
    private ListPreference    mUpdateCheck;
    private ListPreference    mRecoveryType;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_UPDATE_PREFERENCES);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_update);

        setHasOptionsMenu(true);

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
