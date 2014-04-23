/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.namelessrom.updatecenter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import org.namelessrom.updatecenter.services.UpdateCheckService;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

import static org.namelessrom.updatecenter.Application.logDebug;

public class UpdateCheckReceiver extends BroadcastReceiver implements Constants {
    private static final String TAG = "UpdateCheckReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        logDebug(TAG + ": " + action);

        // Load the required settings from preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int updateFrequency = prefs.getInt(UPDATE_CHECK_PREF, UPDATE_FREQ_WEEKLY);

        // Check if we are set to manual updates and don't do anything
        if (updateFrequency == UPDATE_FREQ_NONE) {
            return;
        }

        // Not set to manual updates, parse the received action

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            // Connectivity has changed
            boolean hasConnection =
                    !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            logDebug("Got connectivity change, has connection: " + hasConnection);
            if (!hasConnection) {
                return;
            }
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // We just booted. Store the boot check state
            prefs.edit().putBoolean(BOOT_CHECK_COMPLETED, false).apply();
        }

        // Handle the actual update check based on the defined frequency
        if (updateFrequency == UPDATE_FREQ_AT_BOOT) {
            boolean bootCheckCompleted = prefs.getBoolean(BOOT_CHECK_COMPLETED, false);
            if (!bootCheckCompleted) {
                logDebug("Start an on-boot check");
                Intent i = new Intent(context, UpdateCheckService.class);
                i.setAction(UpdateCheckService.ACTION_CHECK);
                context.startService(i);
            } else {
                // Nothing to do
                logDebug("On-boot update check was already completed.");
            }
        } else if (updateFrequency > 0) {
            logDebug("Scheduling future, repeating update checks.");
            Helper.scheduleUpdateService(context, updateFrequency * 1000);
        }

        // TODO: preferences
        Helper.scheduleAutoUpdate(context, UPDATE_FREQ_TWICE_DAILY * 1000);
    }
}
