package org.namelessrom.updatecenter;

import android.os.SystemProperties;
import android.util.Log;

import org.namelessrom.updatecenter.utils.Constants;

/**
 * Created by alex on 17.02.14.
 */
public class Application extends android.app.Application {

    public static boolean IS_DEBUG = false;

    @Override
    public void onCreate() {
        super.onCreate();

        final String isDebug = SystemProperties.get("ro.nameless.debug");
        IS_DEBUG = (isDebug != null && isDebug.equals("1"));

    }

    public static void logDebug(String msg) {
        if (IS_DEBUG) {
            Log.e("LOGDEBUG", msg);
        }
    }

}
