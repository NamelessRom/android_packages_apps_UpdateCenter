package org.namelessrom.updatecenter.fragments.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.squareup.otto.Subscribe;

import org.json.JSONObject;
import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.VolleyResponseEvent;
import org.namelessrom.updatecenter.net.HttpHandler;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachPreferenceFragment;

public class MainPreferenceFragment extends AttachPreferenceFragment implements Constants {

    private Preference mApiServer;

    private static final Object  mLockObject = new Object();
    private              boolean mIsUpdating = false;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, Constants.ID_PREFERENCES); }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

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
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mApiServer == preference) {
            mApiServer.setEnabled(false);
            synchronized (mLockObject) {
                if (!mIsUpdating) {
                    mIsUpdating = true;
                    HttpHandler.getVolley(API_URL, HttpHandler.TYPE_GENERAL);
                }
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Subscribe
    public void onVolleyResponseEvent(final VolleyResponseEvent event) {
        if (event == null) return;

        String result = null;
        if (!event.isError()) {
            result = event.getOutput();
            if (result != null && !result.isEmpty()) {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    result = jsonObject.getString(TAG_API_VERSION);
                } catch (Exception ignored) { result = null; }
            } else { result = null; }
        }

        if (mApiServer != null) {
            if (result == null) result = getString(R.string.unknown);
            mApiServer.setSummary(getString(R.string.version, result));
            mApiServer.setEnabled(true);
        }

        mIsUpdating = false;
    }

}
