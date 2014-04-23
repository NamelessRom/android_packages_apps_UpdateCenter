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
import android.widget.ListView;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachListFragment;
import org.namelessrom.updatecenter.widgets.adapters.AppListAdapter;

public class AppListFragment extends AttachListFragment implements Constants {

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, Constants.ID_ROM_UPDATE);
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
        setListAdapter(new AppListAdapter(getActivity()));
        final ListView listView = getListView();
        if (listView != null) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            listView.setDividerHeight(4);
            listView.setDivider(getResources().getDrawable(R.drawable.transparent));
            listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        }
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

}
