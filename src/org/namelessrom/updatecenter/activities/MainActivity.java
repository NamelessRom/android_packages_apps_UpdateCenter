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

package org.namelessrom.updatecenter.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.fragments.UpdateFragment;
import org.namelessrom.updatecenter.fragments.WelcomeFragment;
import org.namelessrom.updatecenter.fragments.preferences.MainPreferenceFragment;
import org.namelessrom.updatecenter.fragments.preferences.UpdatePreferenceFragment;
import org.namelessrom.updatecenter.services.AutoUpdater;
import org.namelessrom.updatecenter.services.UpdateCheckService;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.adapters.MenuListArrayAdapter;

public class MainActivity extends Activity implements Constants, AdapterView.OnItemClickListener,
        SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener {

    public static SlidingMenu mSlidingMenu;

    private int mTitle            = R.string.app_name;
    private int mFragmentTitle    = R.string.app_name;
    private int mSubFragmentTitle = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Helper.createDirectories();

        final View v = getLayoutInflater().inflate(R.layout.menu_list, null, false);
        final ListView mMenuList = (ListView) v.findViewById(R.id.navbarlist);

        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setBackground(getResources().getDrawable(R.drawable.bg_menu_dark));
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(v);

        /*final View vv = getLayoutInflater().inflate(R.layout.menu_prefs, null, false);
        mSlidingMenu.setSecondaryMenu(vv);
        mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);*/

        final MenuListArrayAdapter mAdapter = new MenuListArrayAdapter(
                this,
                R.layout.menu_main_list_item,
                getResources().getStringArray(R.array.menu_entries),
                MENU_ICONS);
        mMenuList.setAdapter(mAdapter);
        mMenuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuList.setOnItemClickListener(this);

        mSlidingMenu.setOnClosedListener(this);
        mSlidingMenu.setOnOpenedListener(this);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment main = new WelcomeFragment();
        /*final Fragment right = new MainPreferenceFragment();*/
        ft.replace(R.id.container, main);
        /*ft.replace(R.id.menu_frame, right);*/
        ft.commit();

        if (prefs.getInt(UPDATE_CHECK_PREF, UPDATE_FREQ_WEEKLY) == UPDATE_FREQ_AT_APP_START) {
            final Intent i = new Intent(this, UpdateCheckService.class);
            i.setAction(UpdateCheckService.ACTION_CHECK);
            this.startService(i);
        }

        if (Helper.isNamelessDebug()) {
            startService(new Intent(MainActivity.this, AutoUpdater.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSubFragmentTitle == -1) {
                    mSlidingMenu.toggle(true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Fragment main;
        /*Fragment right;*/

        switch (i) {
            default:
            case ID_UC:
                main = new WelcomeFragment();
                /*right = new MainPreferenceFragment();*/
                break;
            case ID_ROM_UPDATE:
                main = new UpdateFragment();
                /*right = new UpdatePreferenceFragment();*/
                break;
            case ID_PREFERENCES:
                main = new MainPreferenceFragment();
                break;
        }

        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.container, main);
        /*ft.replace(R.id.menu_frame, right);*/

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    @Subscribe
    public void onSectionAttached(final SectionAttachedEvent event) {
        final int id = event.getId();
        switch (id) {
            case ID_RESTORE:
                if (mSubFragmentTitle != -1) {
                    mTitle = mSubFragmentTitle;
                } else {
                    mTitle = mFragmentTitle;
                }
                break;
            case ID_RESTORE_FROM_SUB:
                mSubFragmentTitle = -1;
                mTitle = mFragmentTitle;
                break;
            case ID_FIRST_MENU:
                mTitle = R.string.menu;
                break;
            case ID_SECOND_MENU:
                mTitle = R.string.preferences;
                break;
            default:
                mTitle = mFragmentTitle = R.string.app_name;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_UC:
                mTitle = mFragmentTitle = R.string.app_name;
                mSubFragmentTitle = -1;
                break;
            case ID_ROM_UPDATE:
                mTitle = mFragmentTitle = R.string.updates;
                mSubFragmentTitle = -1;
                break;
            case ID_ROM_UPDATE_PREFERENCES:
                mTitle = mSubFragmentTitle = R.string.updates;
                break;
            case ID_PREFERENCES:
                mTitle = mFragmentTitle = R.string.preferences;
                mSubFragmentTitle = -1;
                break;
        }

        restoreActionBar();
    }

    private void restoreActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public void onOpened() {
        int id;
        if (mSlidingMenu.isMenuShowing() && !mSlidingMenu.isSecondaryMenuShowing()) {
            id = ID_FIRST_MENU;
        } else {
            id = ID_SECOND_MENU;
        }
        onSectionAttached(new SectionAttachedEvent(id));
    }

    @Override
    public void onClosed() {
        onSectionAttached(new SectionAttachedEvent(ID_RESTORE));
    }

    @Subscribe
    public void onSubFragmentEvent(final SubFragmentEvent event) {
        if (event == null) return;

        Fragment main = null;
        final int id = event.getId();

        /*Fragment right = HelpFragment.newInstance(id);*/

        switch (id) {
            case ID_ROM_UPDATE_PREFERENCES:
                main = new UpdatePreferenceFragment();
                break;
            default:
                break;
        }

        if (main == null /*|| right == null*/) return;

        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.container, main);
        /*ft.replace(R.id.menu_frame, right);*/
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
    }
}
