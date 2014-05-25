package org.namelessrom.updatecenter.fragments.updates;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.widgets.AttachPreferenceFragment;

public class UpdatePreferenceFragment extends AttachPreferenceFragment implements Constants,
        Preference.OnPreferenceChangeListener {

    private SharedPreferences mPrefs;
    private ListPreference    mUpdateChannel;
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

        mPrefs = PreferenceManager.getDefaultSharedPreferences(Application.sApplicationContext);
        int tmp;

        mUpdateChannel = (ListPreference) findPreference(Constants.PREF_UPDATE_CHANNEL);
        if (mUpdateChannel != null) {
            tmp = mPrefs.getInt(Constants.PREF_UPDATE_CHANNEL, Constants.UPDATE_CHANNEL_ALL);
            mUpdateChannel.setValue(String.valueOf(tmp));
            setSummary(mUpdateChannel, tmp);
            mUpdateChannel.setOnPreferenceChangeListener(this);
        }

        mUpdateCheck = (ListPreference) findPreference(Constants.UPDATE_CHECK_PREF);
        if (mUpdateCheck != null) {
            tmp = mPrefs.getInt(Constants.UPDATE_CHECK_PREF, Constants.UPDATE_FREQ_WEEKLY);
            mUpdateCheck.setValue(String.valueOf(tmp));
            setSummary(mUpdateCheck, tmp);
            mUpdateCheck.setOnPreferenceChangeListener(this);
        }

        mRecoveryType = (ListPreference) findPreference(Constants.PREF_RECOVERY_TYPE);
        if (mRecoveryType != null) {
            tmp = mPrefs.getInt(Constants.PREF_RECOVERY_TYPE, Constants.RECOVERY_TYPE_BOTH);
            mRecoveryType.setValue(String.valueOf(tmp));
            setSummary(mRecoveryType, tmp);
            mRecoveryType.setOnPreferenceChangeListener(this);
        }
    }

    private void setSummary(final Preference preference, final int value) {
        int resId = R.string.unknown;
        if (mUpdateChannel == preference) {
            switch (value) {
                case Constants.UPDATE_CHANNEL_ALL:
                    resId = R.string.all;
                    break;
                case Constants.UPDATE_CHANNEL_NIGHTLY:
                    resId = R.string.nightly;
                    break;
                case Constants.UPDATE_CHANNEL_WEEKLY:
                    resId = R.string.check_weekly;
                    break;
            }
        } else if (mUpdateCheck == preference) {
            switch (value) {
                case Constants.UPDATE_FREQ_NONE:
                    resId = R.string.check_manual;
                    break;
                case Constants.UPDATE_FREQ_AT_APP_START:
                    resId = R.string.check_on_app_start;
                    break;
                case Constants.UPDATE_FREQ_AT_BOOT:
                    resId = R.string.check_on_boot;
                    break;
                case Constants.UPDATE_FREQ_TWICE_DAILY:
                    resId = R.string.check_twice_daily;
                    break;
                case Constants.UPDATE_FREQ_DAILY:
                    resId = R.string.check_daily;
                    break;
                case Constants.UPDATE_FREQ_WEEKLY:
                    resId = R.string.check_weekly;
                    break;
                case Constants.UPDATE_FREQ_BI_WEEKLY:
                    resId = R.string.check_bi_weekly;
                    break;
                case Constants.UPDATE_FREQ_MONTHLY:
                    resId = R.string.check_monthly;
                    break;
            }
        } else if (mRecoveryType == preference) {
            switch (value) {
                case Constants.RECOVERY_TYPE_BOTH:
                    resId = R.string.recovery_type_both;
                    break;
                case Constants.RECOVERY_TYPE_CWM:
                    resId = R.string.recovery_type_cwm;
                    break;
                case Constants.RECOVERY_TYPE_OPEN:
                    resId = R.string.recovery_type_open;
                    break;
            }
        }

        preference.setSummary(resId);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mUpdateChannel == preference) {
            final int value = Integer.valueOf((String) newValue);
            mPrefs.edit().putInt(Constants.PREF_UPDATE_CHANNEL, value).apply();
            setSummary(preference, value);
            return true;
        } else if (mUpdateCheck == preference) {
            final int value = Integer.valueOf((String) newValue);
            mPrefs.edit().putInt(Constants.UPDATE_CHECK_PREF, value).apply();
            Helper.scheduleUpdateService(getActivity(), value * 1000);
            setSummary(preference, value);
            return true;
        } else if (mRecoveryType == preference) {
            final int value = Integer.valueOf((String) newValue);
            mPrefs.edit().putInt(Constants.PREF_RECOVERY_TYPE, value).apply();
            setSummary(preference, value);
            return true;
        }

        return false;
    }

}
