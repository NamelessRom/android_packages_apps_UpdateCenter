/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.namelessrom.updatecenter.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

public class DownloadService extends IntentService implements Constants {
    private static final String TAG = DownloadService.class.getSimpleName();

    private static final String EXTRA_UPDATE_INFO = "update_info";

    private SharedPreferences mPrefs;

    public static void start(Context context, UpdateInfo ui) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(EXTRA_UPDATE_INFO, (Parcelable) ui);
        context.startService(intent);
    }

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        UpdateInfo ui = intent.getParcelableExtra(EXTRA_UPDATE_INFO);

        if (ui != null) {
            mPrefs.edit()
                    .putLong(DOWNLOAD_ID, enqueueDownload(ui.getUpdateUrl(), ui.getUpdateName()))
                    .commit();
        }
    }

    private long enqueueDownload(String downloadUrl, String updateName) {
        final DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

        request.setTitle(getString(R.string.app_name));
        request.setDescription(updateName);
        request.setDestinationInExternalPublicDir(UPDATE_FOLDER, updateName + ".zip");
        request.setAllowedOverRoaming(false);
        request.setVisibleInDownloadsUi(true); // TODO: change to false once cancel is handled

        // TODO: this could/should be made configurable
        request.setAllowedOverMetered(true);

        return dm.enqueue(request);
    }
}
