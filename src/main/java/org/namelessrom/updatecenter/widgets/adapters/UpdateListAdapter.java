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
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.fragments.dialogs.ChangelogDialogFragment;
import org.namelessrom.updatecenter.fragments.updates.UpdateDetailsFragment;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.widgets.cards.UpdateCard;

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

    @Override
    public int getCount() { return mUpdateInfos.size(); }

    @Override
    public Object getItem(int i) { return mUpdateInfos.get(i); }

    @Override
    public long getItemId(int i) { return 0; }

    private static class ViewHolder {
        private TextView mTitle;
        private TextView mInfo;
        private TextView mChannel;
        private TextView mState;

        private View mOverflow;

        private int mUpdateState;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final View v;
        final ViewHolder viewHolder;

        if (convertView == null) {
            v = new UpdateCard(mContext);

            viewHolder = new ViewHolder();
            viewHolder.mTitle = (TextView) v.findViewById(R.id.updateTitle);
            viewHolder.mInfo = (TextView) v.findViewById(R.id.updateInfo);
            viewHolder.mChannel = (TextView) v.findViewById(R.id.updateChannel);
            viewHolder.mState = (TextView) v.findViewById(R.id.updateState);

            viewHolder.mOverflow = ((UpdateCard) v).getOverflow();

            v.setTag(viewHolder);
        } else {
            v = convertView;
            viewHolder = (ViewHolder) v.getTag();
        }

        final UpdateInfo updateInfo = mUpdateInfos.get(position);
        final String updateChannel = updateInfo.getUpdateChannelShort();

        // TODO: pictures?
        viewHolder.mChannel.setText(updateInfo.getUpdateChannelShort());

        final String timeStamp = updateInfo.getUpdateTimeStamp();
        viewHolder.mTitle.setText(timeStamp);
        viewHolder.mTitle.setSelected(true);

        final String fileName = updateInfo.getUpdateName();
        viewHolder.mInfo.setText(fileName);
        viewHolder.mInfo.setSelected(true);

        if (!updateChannel.equals("?")) {
            viewHolder.mUpdateState = Helper.isUpdateDownloaded(fileName)
                    ? Constants.UPDATE_DOWNLOADED : 0;
            DownloadItem tmpDownloadItem = null;

            if (Application.mDownloadItems != null) {
                for (final DownloadItem item : Application.mDownloadItems) {
                    if (item.getMd5().equals(updateInfo.getUpdateMd5())) {
                        if (item.getCompleted().equals("0")) {
                            tmpDownloadItem = item;
                            viewHolder.mUpdateState = Constants.UPDATE_DOWNLOADING;
                            mUpdateInfos.set(position, updateInfo.setDownloading(true));
                            break;
                        }
                    }
                }
            }

            final DownloadItem downloadItem = tmpDownloadItem;
            viewHolder.mOverflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(viewHolder, updateInfo, downloadItem);
                }
            });
            ((UpdateCard) v).setOverflow(View.VISIBLE);
        } else {
            ((UpdateCard) v).setOverflow(View.GONE);
        }

        int textId;
        switch (viewHolder.mUpdateState) {
            case Constants.UPDATE_DOWNLOADING:
                textId = R.string.downloading;
                break;
            case Constants.UPDATE_DOWNLOADED:
                textId = R.string.downloaded;
                break;
            case Constants.UPDATE_INSTALLED:
                textId = R.string.installed;
                break;
            default:
                textId = 0;
                break;
        }

        if (textId != 0) {
            viewHolder.mState.setVisibility(View.VISIBLE);
            viewHolder.mState.setText(textId);
        } else {
            viewHolder.mState.setVisibility(View.INVISIBLE);
        }

        ((UpdateCard) v).setOnCardClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Fragment f = new UpdateDetailsFragment();
                final Bundle bundle = new Bundle(1);
                bundle.putSerializable(UpdateDetailsFragment.ARG_UPDATE_INFO, updateInfo);
                f.setArguments(bundle);
                BusProvider.getBus().post(new SubFragmentEvent(ID_UPDATE_DETAILS, f));
            }
        });

        ((UpdateCard) v).setOnCardLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                if (!updateChannel.equals("?")) {
                    DialogFragment f = new ChangelogDialogFragment();
                    Bundle b = new Bundle();
                    b.putString(ChangelogDialogFragment.BUNDLE_URL,
                            updateInfo.getUpdateUrl()
                                    .replace("/download", ".changelog/download")
                    );
                    f.setArguments(b);
                    f.show(mContext.getFragmentManager(), "changelogdialog");
                }
                return true;
            }
        });

        return v;
    }

    private void showPopup(final ViewHolder viewHolder, final UpdateInfo updateInfo,
            final DownloadItem item) {
        final AlertDialog dialog = getDialog(viewHolder.mUpdateState, updateInfo, item);

        final PopupMenu popupMenu = new PopupMenu(mContext, viewHolder.mOverflow);
        Menu menu = popupMenu.getMenu();
        popupMenu.getMenuInflater().inflate(R.menu.popup_updates_overflow, menu);
        setupMenu(viewHolder.mUpdateState, menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                dialog.show();
                return true;
            }
        });
        popupMenu.show();
    }

    private void setupMenu(final int state, final Menu menu) {
        switch (state) {
            case Constants.UPDATE_DOWNLOADED:
                menu.removeItem(R.id.update_download);
                menu.removeItem(R.id.update_download_cancel);

                break;
            case Constants.UPDATE_DOWNLOADING:
                menu.removeItem(R.id.update_install);
                menu.removeItem(R.id.update_download);
                break;
            default:
                menu.removeItem(R.id.update_install);
                menu.removeItem(R.id.update_download_cancel);
                break;
        }
    }

    private AlertDialog getDialog(final int state, final UpdateInfo updateInfo,
            final DownloadItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        int titleId;
        String message;
        switch (state) {
            case Constants.UPDATE_DOWNLOADED:
                titleId = R.string.not_action_install_update;
                message = mContext.getString(R.string.not_download_install_notice,
                        updateInfo.getUpdateName());
                builder.setPositiveButton(R.string.not_action_install_update,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                try {
                                    Helper.triggerUpdate(mContext,
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
                    message = mContext.getString(
                            R.string.cancel_download_question, updateInfo.getUpdateName());
                    builder.setPositiveButton(R.string.cancel_download,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final DownloadManager downloadManager = (DownloadManager)
                                            mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                                    downloadManager.remove(Long.parseLong(item.getDownloadId()));

                                    final DatabaseHandler db =
                                            DatabaseHandler.getInstance(mContext);
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
                    message = mContext.getString(R.string.error_occured);
                }
                break;
            default:
                titleId = R.string.not_action_download;
                message = mContext.getString(R.string.not_download_notice,
                        updateInfo.getUpdateName());
                builder.setPositiveButton(R.string.not_action_download,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i1) {
                                final Intent i = new Intent(mContext, DownloadReceiver.class);
                                i.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
                                i.putExtra(DownloadReceiver.EXTRA_UPDATE_INFO,
                                        (Parcelable) updateInfo);
                                mContext.sendBroadcast(i);
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
