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

package org.namelessrom.updatecenter.fragments.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.IdEvent;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.events.VolleyResponseEvent;
import org.namelessrom.updatecenter.net.HttpHandler;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.AttachFragment;
import org.namelessrom.updatecenter.widgets.adapters.AppListAdapter;

public class AppListFragment extends AttachFragment implements Constants,
        AdapterView.OnItemClickListener {

    private boolean isLoaded = false;

    private ListView     mListView;
    private LinearLayout mLoading;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, Constants.ID_ROM_UPDATE);
        BusProvider.getBus().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View v = inflater.inflate(R.layout.fragment_app_list, container, false);

        assert v != null;

        mListView = (ListView) v.findViewById(R.id.listView);
        mLoading = (LinearLayout) v.findViewById(R.id.loading);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HttpHandler.getVolley(Constants.APP_COUNT_URL, HttpHandler.TYPE_GENERAL);
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
                BusProvider.getBus().post(new SubFragmentEvent(ID_ROM_UPDATE_PREFERENCES));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final AppData appData = (AppData) adapterView.getItemAtPosition(i);
        if (appData != null) {
            // TODO: launch fragment
        }
    }

    @Subscribe
    public void onIdEvent(final IdEvent event) {
        if (event == null || isLoaded) return;

        mLoading.setVisibility(View.GONE);
        isLoaded = true;
    }

    @Subscribe
    public void onVolleyResponseEvent(final VolleyResponseEvent event) {
        if (event == null) return;

        int count;
        try {
            count = new JSONObject(event.getOutput()).getInt("count");
        } catch (Exception e) { count = 0; }

        mListView.setAdapter(new AppListAdapter(getActivity(), count));
        mListView.setOnItemClickListener(this);
    }
}
