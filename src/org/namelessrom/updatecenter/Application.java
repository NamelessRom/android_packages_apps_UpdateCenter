package org.namelessrom.updatecenter;

import android.util.Log;

/**
 * Created by alex on 17.02.14.
 */
public class Application extends android.app.Application {

    public static boolean IS_DEBUG     = false;
    public static boolean IS_LOG_DEBUG = false;

    // TODO: update every time the supported api version changes
    public static final String API_CLIENT = "2.1";

    @Override
    public void onCreate() {
        super.onCreate();

        /*final String isDebug = SystemProperties.get("ro.nameless.debug");
        IS_DEBUG = (isDebug != null && isDebug.equals("1"));*/

        logDebug("Debugging enabled!");

    }

    public static void logDebug(String msg) {
        if (IS_LOG_DEBUG) {
            Log.e("UpdateCenter", msg);
        }
    }

}
