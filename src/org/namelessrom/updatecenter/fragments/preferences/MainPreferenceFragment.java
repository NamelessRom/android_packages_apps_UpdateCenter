package org.namelessrom.updatecenter.fragments.preferences;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;

import org.json.JSONObject;
import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.net.HttpHandler;
import org.namelessrom.updatecenter.utils.Constants;

public class MainPreferenceFragment extends PreferenceFragment implements Constants {

    private Preference mVersion;
    private Preference mApiClient;
    private Preference mApiServer;

    private static final Object  mLockObject = new Object();
    private              boolean mIsUpdating = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);

        final Context context = getActivity();

        mVersion = findPreference("prefs_version");
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

        mApiClient = findPreference("prefs_api_client");
        if (mApiClient != null) {
            mApiClient.setEnabled(false);
            mApiClient.setSummary(getString(R.string.version_dynamically, Application.API_CLIENT));
        }

        mApiServer = findPreference("prefs_api_server");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mApiServer == preference) {
            mApiServer.setEnabled(false);
            synchronized (mLockObject) {
                new ServerApiChecker().execute();
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class ServerApiChecker extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            if (mIsUpdating) return null;

            mIsUpdating = true;

            String version;
            String result;
            try {
                result = HttpHandler.get(API_URL);
            } catch (Exception ignored) { result = null; }

            if (result != null && !result.isEmpty()) {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    version = jsonObject.getString(TAG_API_VERSION);
                } catch (Exception ignored) { version = null; }
            } else { version = null; }

            return version;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mApiServer != null) {
                if (result == null) result = getString(R.string.unknown);
                mApiServer.setSummary(getString(R.string.version_dynamically, result));
                mApiServer.setEnabled(true);
            }

            mIsUpdating = false;
        }
    }

}
