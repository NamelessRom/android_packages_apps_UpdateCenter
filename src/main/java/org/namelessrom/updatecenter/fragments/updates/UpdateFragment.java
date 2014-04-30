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

package org.namelessrom.updatecenter.fragments.updates;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.events.UpdateCheckDoneEvent;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.services.UpdateCheckService;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachListFragment;
import org.namelessrom.updatecenter.widgets.adapters.UpdateListAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static org.namelessrom.updatecenter.Application.logDebug;

public class UpdateFragment extends AttachListFragment implements OnRefreshListener, Constants {

    private List<UpdateInfo> mTitles    = new ArrayList<UpdateInfo>();
    private List<UpdateInfo> mTmpTitles = new ArrayList<UpdateInfo>();
    //
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, Constants.ID_UPDATE);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        final ListView listView = getListView();
        if (listView != null) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            listView.setDividerHeight(4);
            listView.setDivider(getResources().getDrawable(R.drawable.transparent));
            listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        }

        final ViewGroup viewGroup = (ViewGroup) view;
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());

        ActionBarPullToRefresh.from(getActivity())
                .insertLayoutInto(viewGroup)
                .theseChildrenArePullable(listView, listView.getEmptyView())
                .listener(this)
                .setup(mPullToRefreshLayout);

        checkForUpdates();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_updates, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                BusProvider.getBus().post(new SubFragmentEvent(ID_UPDATE_PREFERENCES));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onRefreshEvent(final RefreshEvent event) {
        if (event == null) {
            return;
        }

        Application.sHandler.removeCallbacks(mRefreshRunnable);
        Application.sHandler.postDelayed(mRefreshRunnable, 250);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onRefreshEvent(new RefreshEvent());
        }
    };

    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                final Activity activity = getActivity();
                if (activity != null) {
                    final UpdateListAdapter adapter = new UpdateListAdapter(activity, mTitles);

                    setListAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

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
            mTitles.add(new UpdateInfo("-", getString(R.string.no_updates_available), ""));
        }

        BusProvider.getBus().post(new RefreshEvent());
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
                                        getString(R.string.update_check_failed),
                                        getString(R.string.data_connection_required))
                        );
                        getTitles();
                    }
                }
            });
        }
    }

}
