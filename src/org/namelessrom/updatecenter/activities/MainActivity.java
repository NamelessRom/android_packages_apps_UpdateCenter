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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.fragments.CenterMainFragment;
import org.namelessrom.updatecenter.services.UpdateCheckService;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

public class MainActivity extends Activity implements Constants {

    //
    public static final String CENTER_MAIN_FRAGMENT_TAG = "center_main_fragment_tag";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Helper.createDirectories();

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CenterMainFragment(), CENTER_MAIN_FRAGMENT_TAG)
                    .commit();
        }

        if (prefs.getInt(UPDATE_CHECK_PREF, UPDATE_FREQ_WEEKLY) == UPDATE_FREQ_AT_APP_START) {
            Intent i = new Intent(this, UpdateCheckService.class);
            i.setAction(UpdateCheckService.ACTION_CHECK);
            this.startService(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //


    @Override
    public void onBackPressed() {
        boolean mCancelBackPress = false;

        CenterMainFragment f = (CenterMainFragment)
                getFragmentManager().findFragmentByTag(CENTER_MAIN_FRAGMENT_TAG);
        if (f != null) {
            mCancelBackPress = f.onFragmentBackPressed();
        }

        if (!mCancelBackPress) {
            super.onBackPressed();
        }
    }
}
