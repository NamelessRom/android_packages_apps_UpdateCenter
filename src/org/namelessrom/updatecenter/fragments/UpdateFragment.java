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

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.adapters.UpdateListAdapter;
import org.namelessrom.updatecenter.utils.classes.HttpHandler;
import org.namelessrom.updatecenter.utils.items.UpdateListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 05.01.14.
 */
public class UpdateFragment extends ListFragment {

    //
    private static String URL = "http://nameless-rom.org:3000/api";
    private static final String CHANNEL_NIGHTLY = "NIGHTLY";
    // JSON Node names
    private static final String TAG_CHANNEL = "channel";
    private static final String TAG_FILENAME = "filename";
    private static final String TAG_MD5SUM = "md5sum";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_CODENAME = "codename";
    private static final String TAG_CHANGELOG = "changelog";
    private static final String TAG_ID = "_id";
    //
    JSONArray mUpdateArray = null;

    //
    List<UpdateListItem> mTitles = new ArrayList<UpdateListItem>();
    List<UpdateListItem> mTmpTitles = new ArrayList<UpdateListItem>();

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        new CheckUpdateTask().execute();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int i, long l) {
        super.onListItemClick(listView, view, i, l);
        if (i == 0) {
            new CheckUpdateTask().execute();
        } else {
            // TODO show dialog
        }
    }

//

    private void getTitles() {
        UpdateListItem item = new UpdateListItem(
                "", getString(R.string.general_press_to_update), ""
        );
        mTitles.add(item);

        for (UpdateListItem mTmpTitle : mTmpTitles) {
            mTitles.add(mTmpTitle);
        }

        UpdateListAdapter adapter = new UpdateListAdapter(getActivity(), mTitles);

        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //

    class CheckUpdateTask extends AsyncTask<Void, Void, Void> {

        final Context mContext = getActivity();
        ProgressDialog mDialog = new ProgressDialog(mContext);

        @Override
        protected void onPreExecute() {
            // Show dialog
            mDialog.setTitle("Checking for updates");
            mDialog.setMessage("Searching for Updates on Channel \"" + "All" + "\"");
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(true);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    CheckUpdateTask.this.cancel(true);
                }
            });
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            if (mTitles != null) {
                mTitles.clear();
            } else {
                mTitles = new ArrayList<UpdateListItem>();
            }
            if (mTmpTitles != null) {
                mTmpTitles.clear();
            } else {
                mTmpTitles = new ArrayList<UpdateListItem>();
            }

            String url;
            url = URL + "/" + CHANNEL_NIGHTLY;
            url = url + "/" + Helper.getDeviceId();
            Log.e("HttpHandler", "Url: " + url);

            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.sendRequest(url, HttpHandler.GET);

            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    mUpdateArray = new JSONArray(jsonStr);

                    if (mUpdateArray.length() == 0) {
                        CheckUpdateTask.this.cancel(true);
                        return null;
                    }

                    // looping through All Contacts
                    for (int i = 0; i < mUpdateArray.length(); i++) {
                        JSONObject c = mUpdateArray.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String channel = c.getString(TAG_CHANNEL);
                        String filename = c.getString(TAG_FILENAME).replace(".zip", "");
                        String md5sum = c.getString(TAG_MD5SUM);

                        if (channel.equals("NIGHTLY")) {
                            channel = "N";
                        }

                        UpdateListItem item = new UpdateListItem(channel, filename, md5sum);

                        // adding contact to contact list
                        mTmpTitles.add(item);
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
            mDialog.hide();
            mTmpTitles.clear();
            mTmpTitles.add(
                    new UpdateListItem("-", getString(R.string.general_no_updates_available), "")
            );
            getTitles();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDialog.hide();
            getTitles();
        }

    }
}
