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
import android.widget.ListView;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.AttachFragment;
import org.namelessrom.updatecenter.widgets.adapters.AppListAdapter;

public class AppListFragment extends AttachFragment implements Constants,
        AdapterView.OnItemClickListener {

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

        final ListView mListView = (ListView) v.findViewById(R.id.listView);
        mListView.setAdapter(new AppListAdapter(getActivity()));
        mListView.setOnItemClickListener(this);

        return v;
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
}
