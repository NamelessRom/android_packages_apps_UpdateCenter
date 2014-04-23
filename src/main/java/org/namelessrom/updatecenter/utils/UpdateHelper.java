package org.namelessrom.updatecenter.utils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;

import static org.namelessrom.updatecenter.Application.logDebug;

/**
 * Created by alex on 30.04.14.
 */
public class UpdateHelper {

    public static AlertDialog getDialog(final Context context, final int state,
            final UpdateInfo updateInfo, final DownloadItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int titleId;
        String message;
        switch (state) {
            case Constants.UPDATE_DOWNLOADED:
                titleId = R.string.not_action_install_update;
                message = context.getString(R.string.not_download_install_notice,
                        updateInfo.getUpdateName());
                builder.setPositiveButton(R.string.not_action_install_update,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                try {
                                    Helper.triggerUpdate(context,
                                            updateInfo.getUpdateName() + ".zip");
                                } catch (Exception exc) {
                                    logDebug("Error: " + exc.getMessage());
                                }
                                dialogInterface.dismiss();
                            }
                        }
                );
                break;
            case Constants.UPDATE_DOWNLOADING:
                if (item != null) {
                    titleId = R.string.cancel_download;
                    message = context.getString(
                            R.string.cancel_download_question, updateInfo.getUpdateName());
                    builder.setPositiveButton(R.string.cancel_download,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final DownloadManager downloadManager = (DownloadManager)
                                            context.getSystemService(Context.DOWNLOAD_SERVICE);
                                    downloadManager.remove(Long.parseLong(item.getDownloadId()));

                                    final DatabaseHandler db =
                                            DatabaseHandler.getInstance(context);
                                    db.deleteItem(item, DatabaseHandler.TABLE_DOWNLOADS);
                                    Application.mDownloadItems =
                                            db.getAllItems(DatabaseHandler.TABLE_DOWNLOADS);
                                    BusProvider.getBus().post(new RefreshEvent());
                                    dialogInterface.dismiss();
                                }
                            }
                    );
                } else {
                    titleId = R.string.error;
                    message = context.getString(R.string.error_occured);
                }
                break;
            default:
                titleId = R.string.not_action_download;
                message = context.getString(R.string.not_download_notice,
                        updateInfo.getUpdateName());
                builder.setPositiveButton(R.string.not_action_download,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                final Intent i = new Intent(context, DownloadReceiver.class);
                                i.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
                                i.putExtra(DownloadReceiver.EXTRA_UPDATE_INFO,
                                        (Parcelable) updateInfo);
                                context.sendBroadcast(i);
                                dialogInterface.dismiss();
                            }
                        }
                );
                break;
        }

        builder.setTitle(titleId);
        builder.setMessage(message);
        builder.setNegativeButton(Constants.UPDATE_DOWNLOADING == state
                        ? R.string.dismiss : android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );

        return builder.create();
    }

}
