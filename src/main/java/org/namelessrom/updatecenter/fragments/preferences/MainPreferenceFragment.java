package org.namelessrom.updatecenter.fragments.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;
import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachPreferenceFragment;

public class MainPreferenceFragment extends AttachPreferenceFragment implements Constants {

    private Preference mApiServer;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, Constants.ID_PREFERENCES); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);

        final Context context = getActivity();

        final Preference mVersion = findPreference("prefs_version");
        if (mVersion != null && context != null) {
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

        final Preference mApiClient = findPreference("prefs_api_client");
        if (mApiClient != null) {
            mApiClient.setSummary(getString(R.string.version, Application.API_CLIENT));
        }

        mApiServer = findPreference("prefs_api_server");
        if (mApiServer != null) {
            mApiServer.setEnabled(false);
            getApiVersion();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mApiServer == preference) {
            mApiServer.setEnabled(false);
            getApiVersion();
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void getApiVersion() {
        Ion.with(this).load(API_URL)
                .asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if (result != null) {
                    try {
                        result = new JSONObject(result).getString(TAG_API_VERSION);
                    } catch (Exception ignored) { result = null; }
                } else { result = null; }

                if (mApiServer != null) {
                    if (result == null) result = getString(R.string.unknown);
                    mApiServer.setSummary(getString(R.string.version, result));
                    mApiServer.setEnabled(true);
                }
            }
        });
    }

}
