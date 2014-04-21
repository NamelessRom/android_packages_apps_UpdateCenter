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
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.namelessrom.updatecenter.net.HttpHandler;
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
        if (intent != null) {
            Log.v(TAG, "Checking for updates...");
            new CheckVersion().execute();
        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class CheckVersion extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean shouldUpdate = false;

            if (!Helper.isOnline(AutoUpdater.this)) return false;

            // Store the last update check time
            PreferenceManager.getDefaultSharedPreferences(AutoUpdater.this).edit()
                    .putLong(LAST_AUTO_UPDATE_CHECK_PREF, new Date().getTime())
                    .apply();

            try {
                final PackageManager pm = getPackageManager();
                final String version = HttpHandler.get(UC_APK_VERSION);
                if (pm != null && version != null && !version.isEmpty()) {
                    final int remoteVersion = Integer.parseInt(version);
                    final int localVersion = pm.getPackageInfo(getPackageName(), 0).versionCode;
                    shouldUpdate = (remoteVersion > localVersion);
                }
            } catch (Exception ignored) { }

            return shouldUpdate;
        }

        @Override
        protected void onPostExecute(final Boolean shouldUpdate) {
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
    }

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
                            final String path = f.getAbsolutePath();
                            final String cmdInstall = String.format("pm install -r %s", path);
                            final String cmdRemove = String.format("rm %s", path);
                            Log.v(TAG, "Executing: " + cmdInstall + ";" + cmdRemove);
                            Helper.runShellCommands(cmdInstall, cmdRemove);
                        }
                    } catch (Exception ignored) { }
                }
            }

            stopSelf();

        }
    };

}
