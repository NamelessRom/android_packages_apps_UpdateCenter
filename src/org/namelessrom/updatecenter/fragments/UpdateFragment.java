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
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.events.UpdateCheckDoneEvent;
import org.namelessrom.updatecenter.services.UpdateCheckService;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.adapters.UpdateListAdapter;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;
import org.namelessrom.updatecenter.widgets.AttachListFragment;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static org.namelessrom.updatecenter.Application.logDebug;

public class UpdateFragment extends AttachListFragment implements OnRefreshListener, Constants {

    public static final int              ID         = 200;
    //
    private             List<UpdateInfo> mTitles    = new ArrayList<UpdateInfo>();
    private             List<UpdateInfo> mTmpTitles = new ArrayList<UpdateInfo>();
    //
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID);
        activity.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        BusProvider.getBus().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.unregisterReceiver(receiver);
            }
        } catch (Exception ignored) { /* Not registered, nothing to do */ }

        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        final ViewGroup viewGroup = (ViewGroup) view;
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                .listener(this)
                .setup(mPullToRefreshLayout);

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }

        checkForUpdates();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                final ListView listView = getListView();
                if (listView != null) {
                    listView.invalidateViews();
                }
            }
        }
    };

//

    private void getTitles() {
        if (mTitles != null) {
            mTitles.clear();
        } else {
            mTitles = new ArrayList<UpdateInfo>();
        }

        for (final UpdateInfo mTmpTitle : mTmpTitles) {
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
            mTitles.add(new UpdateInfo("-", getString(R.string.general_no_updates_available), ""));
        }

        final UpdateListAdapter adapter = new UpdateListAdapter(getActivity(), mTitles);

        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        mPullToRefreshLayout.setRefreshComplete();
    }

    private void checkForUpdates() {
        final Activity activity = getActivity();
        if (activity != null) {
            final Intent i = new Intent(activity, UpdateCheckService.class);
            i.setAction(UpdateCheckService.ACTION_CHECK_UI);
            activity.startService(i);
        }
    }

    @Override
    public void onRefreshStarted(final View view) {
        checkForUpdates();
    }

    @Subscribe
    public void onUpdateCheckDone(final UpdateCheckDoneEvent event) {
        final Activity activity = getActivity();
        if (isAdded() && activity != null) {
            final boolean isSuccess = event.isSuccess();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logDebug("onUpdateCheckDone: " + (isSuccess ? "true" : "false"));
                    if (isSuccess) {
                        mTmpTitles = event.getUpdates();
                        getTitles();
                    } else {
                        mTmpTitles.clear();
                        mTmpTitles.add(
                                new UpdateInfo("-",
                                        getString(R.string.general_no_updates_available),
                                        "-")
                        );
                        getTitles();
                    }
                }
            });
        }
    }

}
