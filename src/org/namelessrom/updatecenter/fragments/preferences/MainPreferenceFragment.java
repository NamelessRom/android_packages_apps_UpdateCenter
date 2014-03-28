package org.namelessrom.updatecenter.fragments.preferences;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;

public class MainPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);

        final Context context = getActivity();

        final Preference mVersion = findPreference("prefs_version");
        if (mVersion != null && context != null) {
            mVersion.setEnabled(false);
            try {
                final PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    final PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
                    mVersion.setTitle(getString(R.string.app_version_name, pInfo.versionName));
                    mVersion.setSummary(getString(R.string.app_version_code, pInfo.versionCode));
                }
            } catch (Exception ignored) {
                final String unknown = getString(R.string.unknown);
                mVersion.setTitle(unknown);
                mVersion.setSummary(unknown);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }
}
