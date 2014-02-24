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
import android.os.Parcelable;
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
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.adapters.UpdateListAdapter;
import org.namelessrom.updatecenter.utils.classes.HttpHandler;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class UpdateFragment extends ListFragment implements OnRefreshListener, Constants {

    //
    private List<UpdateInfo> mTitles = new ArrayList<UpdateInfo>();
    private List<UpdateInfo> mTmpTitles = new ArrayList<UpdateInfo>();
    //
    private PullToRefreshLayout mPullToRefreshLayout;

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

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                getListView().invalidateViews();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

//

    private void getTitles() {
        if (mTitles != null) {
            mTitles.clear();
        } else {
            mTitles = new ArrayList<UpdateInfo>();
        }

        for (UpdateInfo mTmpTitle : mTmpTitles) {
            mTitles.add(mTmpTitle);
        }

        if (Application.IS_DEBUG) {
            Time now = new Time();
            now.setToNow();
            mTitles.add(new UpdateInfo("-", "Date: " + now.toString()));
            for (int i = 0; i < 20; i++) {
                mTitles.add(new UpdateInfo("-", "---"));
            }
        }

        if (mTitles.size() == 0) {
            mTitles.add(
                    new UpdateInfo("-", getString(R.string.general_no_updates_available), "")
            );
        }

        UpdateListAdapter adapter = new UpdateListAdapter(getActivity(), mTitles);

        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        mPullToRefreshLayout.setRefreshComplete();
    }

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
                mTmpTitles = new ArrayList<UpdateInfo>();
            }

            if (!Helper.isOnline(getActivity())) {
                cancel(true);
            }

            // Builds: BaseUrl + Channel + DeviceId
            final String url = ROM_URL + "/" + CHANNEL_NIGHTLY + "/"
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
                            UpdateInfo item = new UpdateInfo(channel, filename, md5sum,
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
                        new UpdateInfo("-",
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
