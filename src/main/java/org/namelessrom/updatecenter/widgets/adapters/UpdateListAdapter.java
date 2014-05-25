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
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.fragments.dialogs.ChangelogDialogFragment;
import org.namelessrom.updatecenter.fragments.updates.UpdateDetailsFragment;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.UpdateHelper;
import org.namelessrom.updatecenter.widgets.cards.UpdateCard;

import java.util.List;

import static butterknife.ButterKnife.findById;

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
    public Object getItem(final int i) { return mUpdateInfos.get(i); }

    @Override
    public long getItemId(final int i) { return 0; }

    private static class ViewHolder {
        private TextView mTitle;
        private TextView mInfo;
        private TextView mChannel;
        private TextView mState;
        private View     mOverflow;
        private int      mUpdateState;

        public ViewHolder(final View v) {
            mTitle = findById(v, R.id.updateTitle);
            mInfo = findById(v, R.id.updateInfo);
            mChannel = findById(v, R.id.updateChannel);
            mState = findById(v, R.id.updateState);
            mOverflow = ((UpdateCard) v).getOverflow();
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = new UpdateCard(mContext);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final UpdateInfo updateInfo = mUpdateInfos.get(position);
        final String updateChannel = updateInfo.getChannelShort();

        // TODO: pictures?
        viewHolder.mChannel.setText(updateChannel);

        String title, info;
        if (!updateChannel.equals("?") && !updateChannel.equals("-")) {
            title = updateInfo.getTimestamp();
            info = updateInfo.getName();
            viewHolder.mUpdateState = Helper.isUpdateDownloaded(info)
                    ? Constants.UPDATE_DOWNLOADED : 0;
            DownloadItem tmpDownloadItem = null;

            if (Application.mDownloadItems != null) {
                for (final DownloadItem item : Application.mDownloadItems) {
                    if (item.getMd5().equals(updateInfo.getMd5())) {
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
            ((UpdateCard) convertView).setOverflow(View.VISIBLE);

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
                viewHolder.mState.setVisibility(View.GONE);
            }

            ((UpdateCard) convertView).setOnCardClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Fragment f = new UpdateDetailsFragment();
                    final Bundle bundle = new Bundle(1);
                    bundle.putSerializable(UpdateDetailsFragment.ARG_UPDATE_INFO, updateInfo);
                    f.setArguments(bundle);
                    BusProvider.getBus().post(new SubFragmentEvent(ID_UPDATE_DETAILS, f));
                }
            });

            ((UpdateCard) convertView).setOnCardLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    DialogFragment f = new ChangelogDialogFragment();
                    Bundle b = new Bundle();
                    b.putString(ChangelogDialogFragment.BUNDLE_URL,
                            updateInfo.getUrl()
                                    .replace("/download", ".changelog/download")
                    );
                    f.setArguments(b);
                    f.show(mContext.getFragmentManager(), "changelogdialog");
                    return true;
                }
            });
        } else {
            title = updateInfo.getMd5();
            info = updateInfo.getName();
            ((UpdateCard) convertView).setOverflow(View.GONE);
            viewHolder.mState.setVisibility(View.GONE);
        }
        viewHolder.mTitle.setText(title);
        viewHolder.mTitle.setSelected(true);

        if (!info.isEmpty()) {
            viewHolder.mInfo.setVisibility(View.VISIBLE);
            viewHolder.mInfo.setText(info);
            viewHolder.mInfo.setSelected(true);
        } else {
            viewHolder.mInfo.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void showPopup(final ViewHolder viewHolder, final UpdateInfo updateInfo,
            final DownloadItem item) {
        final AlertDialog dialog = UpdateHelper.getDialog(mContext, viewHolder.mUpdateState,
                updateInfo, item);

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

}
