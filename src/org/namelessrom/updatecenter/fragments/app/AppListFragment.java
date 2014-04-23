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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fima.cardsui.views.CardUI;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.net.requests.GsonRequest;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.AttachFragment;
import org.namelessrom.updatecenter.widgets.cards.AppCard;

public class AppListFragment extends AttachFragment implements Constants,
        Response.Listener<AppData[]>, Response.ErrorListener {

    private CardUI mCardUi;

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

        mCardUi = (CardUI) v.findViewById(R.id.cardUi);
        mCardUi.setSwipeable(false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Application.addToRequestQueue(new GsonRequest(Request.Method.GET,
                "https://api.nameless-rom.org/app", AppData[].class, this, this));
    }

    @Override
    public void onResponse(AppData[] response) {
        if (response != null) {
            for (final AppData appData : response) {
                final AppCard card = new AppCard(appData.getAppId(), appData.getTitle(),
                        appData.getDescription());
                mCardUi.addCard(card);
            }
            mCardUi.refresh();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // isLoading = false;
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
