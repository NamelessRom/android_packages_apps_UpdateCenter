/*
 * Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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

package org.namelessrom.updatecenter.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.fragments.dialogs.ChangelogDialogFragment;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.adapters.UpdateListAdapter;
import org.namelessrom.updatecenter.utils.classes.HttpHandler;
import org.namelessrom.updatecenter.utils.items.UpdateListItem;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class UpdateFragment extends ListFragment implements OnRefreshListener {

    //
    private static final String URL = "http://nameless-rom.org:3000/api";
    private static final String CHANNEL_NIGHTLY = "NIGHTLY";
    // JSON Node names
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_FILENAME = "filename";
    private static final String TAG_MD5SUM = "md5sum";
    private static final String TAG_TIMESTAMP = "timestamp";
    // private static final String TAG_CODENAME = "codename";
    private static final String TAG_CHANGELOG = "changelog";
    private static final String TAG_URL = "downloadurl";
    //
    private long mEnqueue;
    private DownloadManager mDownloadManager;

    //
    private List<UpdateListItem> mTitles = new ArrayList<UpdateListItem>();
    private List<UpdateListItem> mTmpTitles = new ArrayList<UpdateListItem>();
    //
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup viewGroup,
                             final Bundle bundle) {
        mDownloadManager = (DownloadManager)
                getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        ViewGroup viewGroup = (ViewGroup) view;
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(this)
                .setup(mPullToRefreshLayout);

        new CheckUpdateTask().execute();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int i, long l) {
        super.onListItemClick(listView, view, i, l);
        if (!mTitles.get(i).getUpdateChannelShort().equals("?")) {
            showUpdateDialog(mTitles.get(i));
        }
        if (Application.IS_DEBUG) {
            Toast.makeText(getActivity(), "ITEM: " + i, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (getActivity() != null && receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception exc) {
            // Not registered, nothing to do
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void showUpdateDialog(final UpdateListItem updateListItem) {
        if (updateListItem == null) return;

        final boolean hasChangelog = !(updateListItem.getUpdateChangeLog().equals("-"));

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_updates);
        dialog.setTitle(getString(R.string.update_title));

        TextView text = (TextView) dialog.findViewById(R.id.dialog_updates_info);
        String tmp = getString(R.string.update_name, updateListItem.getUpdateName()) + "\n";
        tmp += getString(R.string.update_channel, updateListItem.getUpdateChannel()) + "\n";
        tmp += getString(R.string.update_timestamp, updateListItem.getUpdateTimeStamp()) + "\n";
        tmp += getString(R.string.update_md5sum, updateListItem.getUpdateMd5()) + "\n";
        text.setText(tmp);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ImageButton changelogButton = (ImageButton) dialog.findViewById(R.id.dialogButtonChangelog);
        if (hasChangelog) {
            changelogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment f = new ChangelogDialogFragment();
                    Bundle b = new Bundle();
                    b.putString(ChangelogDialogFragment.BUNDLE_FILENAME
                            , updateListItem.getUpdateName() + ".zip.changelog");
                    b.putString(ChangelogDialogFragment.BUNDLE_URL
                            , updateListItem.getUpdateUrl()
                            .replace("/download", ".changelog/download"));
                    f.setArguments(b);
                    f.show(getChildFragmentManager(), "changelogdialog");
                }
            });
        } else {
            changelogButton.setEnabled(false);
        }

        ImageButton updateButton = (ImageButton) dialog.findViewById(R.id.dialogButtonDownload);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.createDirectories();
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(updateListItem.getUpdateUrl()));
                request.setDestinationInExternalPublicDir("/UpdateCenter",
                        updateListItem.getUpdateName() + ".zip");
                mEnqueue = mDownloadManager.enqueue(request);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

//

    private void getTitles() {
        if (mTitles != null) {
            mTitles.clear();
        } else {
            mTitles = new ArrayList<UpdateListItem>();
        }

        for (UpdateListItem mTmpTitle : mTmpTitles) {
            mTitles.add(mTmpTitle);
        }

        if (Application.IS_DEBUG) {
            Time now = new Time();
            now.setToNow();
            mTitles.add(new UpdateListItem("-", "Date: " + now.toString()));
            for (int i = 0; i < 20; i++) {
                mTitles.add(new UpdateListItem("-", "---"));
            }
        }

        if (mTitles.size() == 0) {
            mTitles.add(
                    new UpdateListItem("-", getString(R.string.general_no_updates_available), "")
            );
        }

        UpdateListAdapter adapter = new UpdateListAdapter(getActivity(), mTitles);

        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        mPullToRefreshLayout.setRefreshComplete();
    }

    //

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                final long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(mEnqueue);
                final Cursor c = mDownloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    final int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        if (downloadId == mEnqueue) {
                            String uriString = c.getString(
                                    c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                            Uri.parse(uriString);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onRefreshStarted(View view) {
        new CheckUpdateTask().execute();
    }

    class CheckUpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            if (mTmpTitles != null) {
                mTmpTitles.clear();
            } else {
                mTmpTitles = new ArrayList<UpdateListItem>();
            }

            // Builds: BaseUrl + Channel + DeviceId
            final String url = URL + "/" + CHANNEL_NIGHTLY + "/"
                    + Helper.readBuildProp("ro.nameless.device");

            int currentDate;

            try {
                currentDate = Integer.parseInt(Helper.readBuildProp("ro.nameless.date"));
            } catch (Exception exc) {
                currentDate = 20140101;
            }
            //Log.e("HttpHandler", "Url: " + url);

            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.sendRequest(url, HttpHandler.GET);

            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONArray mUpdateArray = new JSONArray(jsonStr);

                    if (mUpdateArray.length() == 0) {
                        CheckUpdateTask.this.cancel(true);
                        return null;
                    }

                    for (int i = 0; i < mUpdateArray.length(); i++) {
                        JSONObject c = mUpdateArray.getJSONObject(i);

                        //final String id = c.getString(TAG_ID);
                        String channel = c.getString(TAG_CHANNEL);
                        final String filename = c.getString(TAG_FILENAME).replace(".zip", "");
                        final String md5sum = c.getString(TAG_MD5SUM);
                        final String urlFile = c.getString(TAG_URL);
                        final String timeStampString = c.getString(TAG_TIMESTAMP);
                        int timeStamp;
                        try {
                            timeStamp = Integer.parseInt(timeStampString);
                        } catch (Exception exc) {
                            timeStamp = 20140102;
                        }
                        final String changeLog = c.getString(TAG_CHANGELOG);

                        if (currentDate < timeStamp) {
                            UpdateListItem item = new UpdateListItem(channel, filename, md5sum,
                                    urlFile, timeStampString, changeLog);

                            mTmpTitles.add(item);
                        }
                    }
                } catch (JSONException e) {
                    CheckUpdateTask.this.cancel(true);
                    e.printStackTrace();
                }
            } else {
                CheckUpdateTask.this.cancel(true);
                Log.e("HttpHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            if (isAdded()) {
                mTmpTitles.clear();
                mTmpTitles.add(
                        new UpdateListItem("-",
                                getString(R.string.general_no_updates_available),
                                "-")
                );
                getTitles();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAdded()) {
                getTitles();
            }
        }

    }
}