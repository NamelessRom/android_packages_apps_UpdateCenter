package org.namelessrom.updatecenter.services;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.utils.AppHelper;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

import java.io.File;
import java.net.URI;
import java.util.Date;

/**
 * Created by alex on 21.04.14.
 */
public class AutoUpdater extends Service implements Constants {

    private static final String TAG = "AutoUpdater";

    private DownloadManager mDownloadManager;
    private long            mDownloadId;

    @Override
    public IBinder onBind(final Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mDownloadCompleteReceiver);
        } catch (Exception ignored) { }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.v(TAG, "Checking for updates...");

        if (intent != null || !Helper.isOnline(AutoUpdater.this)) {
            PreferenceManager.getDefaultSharedPreferences(AutoUpdater.this).edit()
                    .putLong(LAST_AUTO_UPDATE_CHECK_PREF, new Date().getTime())
                    .apply();

            Ion.with(AutoUpdater.this)
                    .load(UC_APK_VERSION)
                    .asString().setCallback(mFutureCallback);
        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    final FutureCallback<String> mFutureCallback = new FutureCallback<String>() {
        @Override
        public void onCompleted(Exception e, String result) {
            boolean shouldUpdate = false;
            try {
                final PackageManager pm = getPackageManager();
                if (result != null && !result.isEmpty()) {
                    Log.v(TAG, "Version: " + result);

                    if (result.startsWith("!")) {
                        shouldUpdate = true;
                    } else if (pm != null) {
                        final int remoteVersion = Integer.parseInt(result);
                        final int localVersion = pm.getPackageInfo(getPackageName(), 0).versionCode;
                        shouldUpdate = (remoteVersion > localVersion);
                    }
                }
            } catch (Exception ignored) { }

            if (shouldUpdate) {
                registerReceiver(mDownloadCompleteReceiver,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(UC_APK));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "uc.apk");

                mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                mDownloadId = mDownloadManager.enqueue(request);
            } else {
                stopSelf();
            }
        }
    };

    final BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            Log.v(TAG, "Download complete!");

            final Cursor c = mDownloadManager.query(
                    new DownloadManager.Query().setFilterById(mDownloadId));

            if (c != null) {
                c.moveToFirst();

                final String downloadUri = c.getString(
                        c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                Log.v(TAG, "downloadUri: " + downloadUri);

                if (downloadUri != null && !downloadUri.isEmpty()) {
                    try {
                        final File f = new File(new URI(downloadUri));
                        if (f.exists()) {
                            AppHelper.installPackage(Application.packageManager, Uri.fromFile(f),
                                    PackageManager.INSTALL_REPLACE_EXISTING);
                        }
                    } catch (Exception ignored) { }
                }
                c.close();
            }

            stopSelf();
        }
    };

}
