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

package org.namelessrom.updatecenter.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.util.Log;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.services.AutoUpdater;
import org.namelessrom.updatecenter.services.UpdateCheckService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.namelessrom.updatecenter.Application.logDebug;

/**
 * Created by alex on 05.01.14.
 */
public class Helper implements Constants {

    private static final int FILE_BUFFER = 512;
    private static Helper sHelper;

    private Helper() {
        // Intentionally left blank
    }

    public static Helper getInstance() {
        if (sHelper == null) {
            sHelper = new Helper();
        }
        return sHelper;
    }

    public static boolean isNamelessDebug() {
        return existsInBuildProp("ro.nameless.debug=1");
    }

    public static boolean existsInBuildProp(final String filter) {
        final File f = new File("/system/build.prop");
        BufferedReader bufferedReader = null;
        if (f.exists() && f.canRead()) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains(filter)) return true;
                }
            } catch (Exception whoops) {
                return false;
            } finally {
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (Exception ignored) {
                    // mepmep
                }
            }
        }
        return false;
    }

    public static String readBuildProp(final String prop) {
        String id = "NULL";
        BufferedReader fileReader = null;
        String tmp;

        try {
            fileReader = new BufferedReader(
                    new FileReader("/system/build.prop"), FILE_BUFFER);

            while ((tmp = fileReader.readLine()) != null) {
                if (tmp.contains(prop)) {
                    id = tmp.replace(prop + "=", "");
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("Helper", "Error: " + e.getMessage());
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (Exception ignored) {
            }
        }

        return id;
    }

    public static void createDirectories() {
        File f = new File(UPDATE_FOLDER_FULL);
        if (!f.exists()) {
            logDebug("Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        }
        f = new File(UPDATE_FOLDER_ADDITIONAL);
        if (!f.exists()) {
            logDebug("Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        }
        f = new File(UPDATE_FOLDER_CHANGELOG);
        if (!f.exists()) {
            logDebug("Created: " + f.getAbsolutePath() + ": " + (f.mkdirs() ? "true" : "false"));
        } else {
            logDebug("Cleaning changelogs.");
            cleanChangelogs(f);
        }
    }

    public static void cleanChangelogs() {
        cleanChangelogs(new File(UPDATE_FOLDER_CHANGELOG));
    }

    public static void cleanChangelogs(final File changelogDir) {
        final int currentDate = getBuildDate();
        final File[] files = changelogDir.listFiles();

        String filename;
        int changelogDate;
        if (files != null) {
            for (final File f : files) {
                if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".changelog")) {
                    filename = f.getAbsolutePath();
                    logDebug("Processing: " + filename);
                    if (filename.startsWith(UPDATE_FOLDER_CHANGELOG)) {
                        final String[] tmp = filename
                                .replace(UPDATE_FOLDER_CHANGELOG + "/", "")
                                .split("-");
                        try {
                            changelogDate = Integer.parseInt(tmp[2]);
                        } catch (Exception ignored) { changelogDate = Integer.MAX_VALUE;}

                        if (changelogDate < currentDate) {
                            logDebug("Deleted file: " + (f.delete() ? "true" : "false"));
                        } else {
                            logDebug("Not deleting changelog");
                        }
                    }
                }
            }
        }
    }

    public static boolean updateIsDownloaded(final String date) {
        final File[] files = new File(UPDATE_FOLDER_FULL).listFiles();
        String filename;
        if (files != null) {
            for (final File f : files) {
                if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                    filename = f.getAbsolutePath();
                    logDebug("Processing: " + filename);
                    if (filename.startsWith(UPDATE_FOLDER_FULL)) {
                        final String[] tmp = filename
                                .replace(UPDATE_FOLDER_FULL + "/", "")
                                .split("-");
                        return date.equals(tmp[2]);
                    }
                }
            }
        }

        return false;
    }

    public static int getBuildDate() {
        return parseDate(Helper.readBuildProp("ro.nameless.date"));
    }

    public static int parseDate(final String timeStampString) {
        int timeStamp;
        try {
            timeStamp = Integer.parseInt(timeStampString);
        } catch (Exception exc) {
            timeStamp = 20140101;
        }
        return timeStamp;
    }

    public static void cancelNotification(Context context) {
        final NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(R.string.not_new_updates_found_title);
        nm.cancel(R.string.not_download_success);
    }

    public static boolean isOnline(Context context) {
        final ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private static String getStorageMountpoint(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = sm.getVolumeList();
        String primaryStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        boolean alternateIsInternal = context.getResources().getBoolean(R.bool.alternateIsInternal);

        if (volumes.length <= 1) {
            // single storage, assume only /sdcard exists
            return "/sdcard";
        }

        for (StorageVolume v : volumes) {
            if (v.getPath().equals(primaryStoragePath)) {
                if (!v.isRemovable() && alternateIsInternal) {
                    return "/emmc";
                }
            }
        }
        // Not found, assume non-alternate
        return "/sdcard";
    }

    public static void scheduleUpdateService(Context context, int updateFrequency) {
        // Load the required settings from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastCheck = prefs.getLong(Constants.LAST_UPDATE_CHECK_PREF, 0);

        // Get the intent ready
        Intent i = new Intent(context, UpdateCheckService.class);
        i.setAction(UpdateCheckService.ACTION_CHECK);
        PendingIntent pi =
                PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (updateFrequency != Constants.UPDATE_FREQ_NONE) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, lastCheck + updateFrequency, updateFrequency,
                    pi);
        }
    }

    public static void scheduleAutoUpdate(Context context, int updateFrequency) {
        // Load the required settings from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastCheck = prefs.getLong(Constants.LAST_AUTO_UPDATE_CHECK_PREF, 0);

        // Get the intent ready
        Intent i = new Intent(context, AutoUpdater.class);
        i.setAction(UpdateCheckService.ACTION_CHECK);
        PendingIntent pi =
                PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (updateFrequency != Constants.UPDATE_FREQ_NONE) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, lastCheck + updateFrequency, updateFrequency,
                    pi);
        }
    }

    public static void runShellCommands(final String... cmds) throws IOException {
        java.lang.Process p = Runtime.getRuntime().exec("sh");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        for (final String s : cmds) {
            writeString(os, s);
        }
        writeString(os, "exit\n");
        os.flush();
    }

    private static void writeString(OutputStream os, String s) throws IOException {
        os.write((s + "\n").getBytes("UTF-8"));
    }

    private static List<String> getFlashAfterUpdateZIPs() {
        final List<String> extras = new ArrayList<String>();
        final File[] files = (new File(UPDATE_FOLDER_ADDITIONAL)).listFiles();
        String filename;

        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
                    filename = f.getAbsolutePath();
                    if (filename.startsWith(UPDATE_FOLDER_FULL)) {
                        extras.add(filename.replace(UPDATE_FOLDER_FULL + "/", ""));
                    }
                }
            }
            Collections.sort(extras);
        }

        return extras;
    }

    private static void createOpenRecoveryScript(final String root, final String filename,
            final List<String> files)
            throws IOException {
        final FileOutputStream os = new FileOutputStream("/cache/recovery/openrecoveryscript",
                false);
        try {
            writeString(os, "set tw_signed_zip_verify 0");
            writeString(os, String.format("install %s", filename));

            for (final String file : files) {
                writeString(os, String.format("install %s", root + file));
            }
            writeString(os, "wipe cache");
        } finally {
            os.close();
        }

        FileUtils.setPermissions("/cache/recovery/openrecoveryscript", 0644, Process.myUid(), 2001);
    }

    private static void createCwmScript(final String root, final String filename,
            final List<String> files) throws IOException {
        final FileOutputStream os = new FileOutputStream("/cache/recovery/extendedcommand", false);
        try {
            writeString(os, String.format("install_zip(\"%s\");", filename));

            for (String file : files) {
                writeString(os, String.format("install_zip(\"%s\");", root + file));
            }
            writeString(os, "run_program(\"/sbin/busybox\", \"rm\", \"-rf\", \"/cache/*\");");
        } finally {
            os.close();
        }

        FileUtils.setPermissions("/cache/recovery/extendedcommand", 0644, Process.myUid(), 2001);
    }

    public static void triggerUpdate(final Context context, final String updateFileName)
            throws IOException {
        // Add the update folder/file name
        // Emulated external storage moved to user-specific paths in 4.2
        final String userPath = Environment.isExternalStorageEmulated()
                ? ("/" + UserHandle.myUserId())
                : "";

        final String rootPath =
                getStorageMountpoint(context) + userPath + "/" + UPDATE_FOLDER + "/";
        final String flashFilename = rootPath + updateFileName;
        final List<String> extras = getFlashAfterUpdateZIPs();

        final int flashType = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_RECOVERY_TYPE, RECOVERY_TYPE_BOTH);
        if (RECOVERY_TYPE_BOTH == flashType) {
            createCwmScript(rootPath, flashFilename, extras);
            createOpenRecoveryScript(rootPath, flashFilename, extras);
        } else if (RECOVERY_TYPE_CWM == flashType) {
            createCwmScript(rootPath, flashFilename, extras);
        } else if (RECOVERY_TYPE_OPEN == flashType) {
            createOpenRecoveryScript(rootPath, flashFilename, extras);
        }

        // Trigger the reboot
        final PowerManager powerManager = (PowerManager)
                context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot("recovery");
    }

}
