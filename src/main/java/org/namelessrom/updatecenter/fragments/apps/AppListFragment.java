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

package org.namelessrom.updatecenter.fragments.apps;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.AppListEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachListFragment;
import org.namelessrom.updatecenter.widgets.adapters.AppListAdapter;

public class AppListFragment extends AttachListFragment implements Constants {

    private AppListAdapter mAdapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, Constants.ID_APP_LIST);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new AppListAdapter(getActivity());
        final ListView listView = getListView();
        if (listView != null) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            listView.setDividerHeight(4);
            listView.setDivider(getResources().getDrawable(R.drawable.transparent));
            listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        }
    }

    @Subscribe
    public void onAppListEvent(final AppListEvent event) {
        if (event == null || !isAdded()) return;

        setListAdapter(mAdapter);
    }

}
