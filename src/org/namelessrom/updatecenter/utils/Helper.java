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

import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by alex on 05.01.14.
 */
public class Helper {

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


    public static String getDeviceId() {
        String id = "NULL";
        BufferedReader fileReader = null;
        String tmp;

        try {
            fileReader = new BufferedReader(
                    new FileReader("/system/build.prop"), FILE_BUFFER);

            while ((tmp = fileReader.readLine()) != null) {
                if (tmp.contains("ro.nameless.device")) {
                    id = tmp.replace("ro.nameless.device=", "");
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
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "UpdateCenter");
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public static void hideSystemUi(final View v) {
        v.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public static void showSystemUi(final View v) {
        v.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
