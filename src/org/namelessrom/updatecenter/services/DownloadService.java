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
import android.net.Uri;
import android.os.Parcelable;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

public class DownloadService extends IntentService implements Constants {
    private static final String TAG = DownloadService.class.getSimpleName();

    private static final String EXTRA_UPDATE_INFO = "update_info";

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
        final UpdateInfo ui = intent.getParcelableExtra(EXTRA_UPDATE_INFO);

        if (ui != null) {
            final long downloadId = enqueueDownload(ui.getUpdateUrl(), ui.getUpdateName());
            final DownloadItem item = new DownloadItem(
                    ui.getUpdateName() + ".zip", String.valueOf(downloadId),
                    ui.getUpdateMd5(), "0");
            DatabaseHandler.getInstance(this).insertOrUpdate(item, DatabaseHandler.TABLE_DOWNLOADS);
            org.namelessrom.updatecenter.Application.mDownloadItems =
                    DatabaseHandler.getInstance(this).getAllItems(DatabaseHandler.TABLE_DOWNLOADS);

            Application.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    BusProvider.getBus().post(new RefreshEvent());
                }
            });
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
