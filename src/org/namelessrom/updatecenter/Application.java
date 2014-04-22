package org.namelessrom.updatecenter;

import android.os.Handler;
import android.util.Log;

import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;

import java.util.List;

/**
 * Created by alex on 17.02.14.
 */
public class Application extends android.app.Application {

    public static final Handler sHandler = new Handler();

    public static boolean IS_DEBUG     = false;
    public static boolean IS_LOG_DEBUG = false;

    // TODO: update every time the supported api version changes
    public static final String API_CLIENT = "2.1";

    public static List<DownloadItem> mDownloadItems;

    @Override
    public void onCreate() {
        super.onCreate();

        /*final String isDebug = SystemProperties.get("ro.nameless.debug");
        IS_DEBUG = (isDebug != null && isDebug.equals("1"));*/

        logDebug("Debugging enabled!");

        mDownloadItems = DatabaseHandler.getInstance(this).getAllItems(
                DatabaseHandler.TABLE_DOWNLOADS);

    }

    public static void logDebug(String msg) {
        if (IS_LOG_DEBUG) {
            Log.e("UpdateCenter", msg);
        }
    }

}
