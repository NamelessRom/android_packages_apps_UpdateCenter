/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.updatecenter.widgets.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.fragments.dialogs.ChangelogDialogFragment;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

import java.io.File;
import java.util.List;

import static org.namelessrom.updatecenter.Application.logDebug;

/**
 * Created by alex on 06.01.14.
 */
public class UpdateListAdapter extends BaseAdapter implements Constants {

    private final Activity         mContext;
    private final List<UpdateInfo> mUpdateInfos;

    public UpdateListAdapter(final Activity context, final List<UpdateInfo> updateInfos) {
        mContext = context;
        mUpdateInfos = updateInfos;
    }

    private class ViewHolder {
        private final TextView    tvChannel;
        private final TextView    tvName;
        private final ImageButton ibAction;

        private ViewHolder(View rootView) {
            tvChannel = (TextView) rootView.findViewById(R.id.list_item_updates_channel);
            tvName = (TextView) rootView.findViewById(R.id.list_item_updates_title);
            ibAction = (ImageButton) rootView.findViewById(R.id.list_item_updates_action);
        }

    }

    @Override
    public int getCount() {
        return mUpdateInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return mUpdateInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.list_item_updates, null, true);

        ViewHolder holder = (ViewHolder) rowView.getTag();
        if (holder == null) {
            holder = new ViewHolder(rowView);
        }

        final UpdateInfo updateInfo = mUpdateInfos.get(position);
        final String updateChannel = mUpdateInfos.get(position).getUpdateChannelShort();

        holder.tvChannel.setText(updateChannel);

        if (!updateChannel.equals("?")) {
            holder.tvChannel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showUpdateDialog(updateInfo);
                }
            });
        }

        final String fileName = mUpdateInfos.get(position).getUpdateName();
        holder.tvName.setText(fileName);
        holder.tvName.setSelected(true);

        final boolean updateExists = new File(UPDATE_FOLDER_FULL + File.separator
                + fileName + ".zip").exists();

        if (!updateChannel.equals("?")) {
            int tmpResId = updateExists ? R.drawable.ic_tab_install : R.drawable.ic_tab_download;
            DownloadItem tmpDownloadItem = null;

            if (Application.mDownloadItems != null) {
                for (final DownloadItem item : Application.mDownloadItems) {
                    if (item.getMd5().equals(updateInfo.getUpdateMd5())) {
                        if (item.getCompleted().equals("0")) {
                            tmpDownloadItem = item;
                            tmpResId = R.drawable.ic_tab_cancel;
                            mUpdateInfos
                                    .set(position, mUpdateInfos.get(position).setDownloading(true));
                            break;
                        }
                    }
                }
            }

            final int resId = tmpResId;
            final DownloadItem downloadItem = tmpDownloadItem;
            holder.ibAction.setImageResource(resId);

            holder.ibAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(resId, updateInfo, downloadItem);
                }
            });
        } else {
            holder.ibAction.setVisibility(View.GONE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!updateChannel.equals("?")) {
                    DialogFragment f = new ChangelogDialogFragment();
                    Bundle b = new Bundle();
                    b.putString(ChangelogDialogFragment.BUNDLE_URL
                            , updateInfo.getUpdateUrl()
                            .replace("/download", ".changelog/download"));
                    f.setArguments(b);
                    f.show(mContext.getFragmentManager(), "changelogdialog");
                }
                if (Application.IS_DEBUG) {
                    Toast.makeText(mContext, "ITEM: " + position, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rowView;
    }

    private void showDialog(final int resId, final UpdateInfo updateInfo, final DownloadItem item) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);

        if (R.drawable.ic_tab_install == resId) {
            dialog.setTitle(R.string.not_action_install_update);
            dialog.setMessage(mContext.getString(R.string.not_download_install_notice,
                    updateInfo.getUpdateName()));
            dialog.setPositiveButton(R.string.not_action_install_update,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i1) {
                            try {
                                Helper.triggerUpdate(mContext, updateInfo.getUpdateName() + ".zip");
                            } catch (Exception exc) {
                                logDebug("Error: " + exc.getMessage());
                            }
                            dialogInterface.dismiss();
                        }
                    }
            );
        } else if (R.drawable.ic_tab_download == resId) {
            dialog.setTitle(R.string.not_action_download);
            dialog.setMessage(mContext.getString(R.string.not_download_notice,
                    updateInfo.getUpdateName()));
            dialog.setPositiveButton(R.string.not_action_download,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i1) {
                            Intent i = new Intent(mContext, DownloadReceiver.class);
                            i.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
                            i.putExtra(DownloadReceiver.EXTRA_UPDATE_INFO, (Parcelable) updateInfo);
                            mContext.sendBroadcast(i);
                            dialogInterface.dismiss();
                        }
                    }
            );
        } else if (R.drawable.ic_tab_cancel == resId) {
            if (item != null) {
                dialog.setTitle(R.string.cancel_download);
                dialog.setMessage(mContext.getString(
                        R.string.cancel_download_question, updateInfo.getUpdateName()));
                dialog.setPositiveButton(R.string.cancel_download,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final DownloadManager downloadManager = (DownloadManager)
                                        mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                                downloadManager.remove(Long.parseLong(item.getDownloadId()));

                                final DatabaseHandler db = DatabaseHandler.getInstance(mContext);
                                db.deleteItem(item, DatabaseHandler.TABLE_DOWNLOADS);
                                Application.mDownloadItems =
                                        db.getAllItems(DatabaseHandler.TABLE_DOWNLOADS);
                                BusProvider.getBus().post(new RefreshEvent());
                                dialogInterface.dismiss();
                            }
                        }
                );
            } else {
                dialog.setTitle(R.string.error);
                dialog.setMessage(R.string.error_occured);
            }
        }

        final int msgId = (R.drawable.ic_tab_cancel == resId
                ? android.R.string.cancel // TODO: Dismiss or whatever
                : android.R.string.cancel);
        dialog.setNegativeButton(msgId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showUpdateDialog(final UpdateInfo updateInfo) {
        if (updateInfo == null) return;

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_updates);
        dialog.setTitle(mContext.getString(R.string.update_title));

        TextView text = (TextView) dialog.findViewById(R.id.dialog_updates_info);
        String tmp = mContext.getString(R.string.update_name, updateInfo.getUpdateName()) + "\n";
        tmp += mContext.getString(R.string.update_channel, updateInfo.getUpdateChannel()) + "\n";
        tmp += mContext.getString(R.string.update_timestamp,
                updateInfo.getUpdateTimeStamp()) + "\n";
        tmp += mContext.getString(R.string.update_md5sum, updateInfo.getUpdateMd5()) + "\n";
        text.setText(tmp);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}