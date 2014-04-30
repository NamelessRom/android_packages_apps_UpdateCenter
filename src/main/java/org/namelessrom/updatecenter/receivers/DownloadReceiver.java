/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.namelessrom.updatecenter.receivers;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.services.DownloadService;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

import java.io.File;
import java.io.IOException;

import static org.namelessrom.updatecenter.Application.logDebug;

public class DownloadReceiver extends BroadcastReceiver implements Constants {

    public static final String ACTION_START_DOWNLOAD =
            "org.namelessrom.updatecenter.action.START_DOWNLOAD";
    public static final String EXTRA_UPDATE_INFO     = "update_info";

    public static final String ACTION_DOWNLOAD_STARTED =
            "org.namelessrom.updatecenter.action.DOWNLOAD_STARTED";

    public static final String ACTION_INSTALL_UPDATE =
            "org.namelessrom.updatecenter.action.INSTALL_UPDATE";
    public static final String EXTRA_FILENAME        = "filename";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (ACTION_START_DOWNLOAD.equals(action)) {
            final UpdateInfo ui = intent.getParcelableExtra(EXTRA_UPDATE_INFO);
            handleStartDownload(context, ui);
        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            handleDownloadComplete(context, prefs, id);
        } else if (ACTION_INSTALL_UPDATE.equals(action)) {
            final StatusBarManager sb = (StatusBarManager)
                    context.getSystemService(Context.STATUS_BAR_SERVICE);
            sb.collapsePanels();

            final String fileName = intent.getStringExtra(EXTRA_FILENAME);
            try {
                Helper.triggerUpdate(context, fileName);
            } catch (IOException e) {
                logDebug("Unable to reboot into recovery mode: " + e.getMessage());
                Toast.makeText(context, R.string.apply_unable_to_reboot_toast,
                        Toast.LENGTH_SHORT).show();
                Helper.cancelNotification(context);
            }
        }
    }

    private void handleStartDownload(Context context, UpdateInfo ui) {
        DownloadService.start(context, ui);
    }

    private void handleDownloadComplete(Context context, SharedPreferences prefs, long id) {
        final long enqueued = prefs.getLong(DOWNLOAD_ID, -1);

        if (enqueued < 0 || id < 0 || id != enqueued) {
            return;
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Query query = new Query();
        query.setFilterById(id);

        final DatabaseHandler db = DatabaseHandler.getInstance(context);
        final DownloadItem item = db.getDownloadItem(String.valueOf(enqueued));
        if (item != null) {
            item.setCompleted("1");
            db.updateItem(item, DatabaseHandler.TABLE_DOWNLOADS);
        }

        Cursor c = dm.query(query);
        if (c == null) {
            return;
        }

        if (!c.moveToFirst()) {
            c.close();
            return;
        }

        final int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        int failureMessageResId = -1;
        File updateFile = null;

        Intent updateIntent = new Intent(context, MainActivity.class);
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            // Get the full path name of the downloaded file and the MD5

            // Strip off the .partial at the end to get the completed file
            final String partialFileFullPath = c.getString(
                    c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            final String completedFileFullPath = partialFileFullPath.replace(".partial", "");

            final File partialFile = new File(partialFileFullPath);
            updateFile = new File(completedFileFullPath);
            partialFile.renameTo(updateFile);

        } else if (status == DownloadManager.STATUS_FAILED) {
            // The download failed, reset
            dm.remove(id);

            failureMessageResId = R.string.unable_to_download_file;
        }

        // Clear the shared prefs
        prefs.edit()
                .remove(Constants.DOWNLOAD_ID)
                .apply();

        c.close();

        // Get the notification ready
        PendingIntent contentIntent = PendingIntent.getActivity(context, 1,
                updateIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (failureMessageResId >= 0) {
            builder.setContentTitle(context.getString(R.string.download_failure));
            builder.setContentText(context.getString(failureMessageResId));
            builder.setTicker(context.getString(R.string.download_failure));
        } else {
            final String updateUiName = updateFile.getName();

            builder.setContentTitle(context.getString(R.string.download_success));
            builder.setContentText(updateUiName);
            builder.setTicker(context.getString(R.string.download_success));

            Notification.BigTextStyle style = new Notification.BigTextStyle();
            style.setBigContentTitle(context.getString(R.string.download_success));
            style.bigText(context.getString(R.string.download_install_notice, updateUiName));
            builder.setStyle(style);

            Intent installIntent = new Intent(context, DownloadReceiver.class);
            installIntent.setAction(ACTION_INSTALL_UPDATE);
            installIntent.putExtra(EXTRA_FILENAME, updateUiName);

            PendingIntent installPi = PendingIntent.getBroadcast(context, 0,
                    installIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_stat_notify_install,
                    context.getString(R.string.reboot_and_install), installPi);
        }

        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(R.string.download_success, builder.build());
    }
}
