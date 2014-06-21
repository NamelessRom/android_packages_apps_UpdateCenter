package org.namelessrom.updatecenter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.utils.Helper;

import java.util.List;

public class Application extends android.app.Application {

    private static final String TAG = Application.class.getName();

    public static final Handler sHandler = new Handler();

    public static final boolean IS_DEBUG     = false;
    public static       boolean IS_LOG_DEBUG = true;

    // TODO: update every time the supported api version changes
    public static final String API_CLIENT = "2.1.3";

    public static Context         sApplicationContext;
    public static PackageManager  packageManager;
    public static DownloadManager downloadManager;

    public static List<DownloadItem> mDownloadItems;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplicationContext = this;
        packageManager = sApplicationContext.getPackageManager();

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (Helper.existsInBuildProp("ro.nameless.debug=1")) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
            IS_LOG_DEBUG = true;
        }

        logDebug(TAG, "Debugging enabled!");

        mDownloadItems = DatabaseHandler.getInstance(sApplicationContext)
                .getAllItems(DatabaseHandler.TABLE_DOWNLOADS);
    }

    public static void logDebug(final String msg) { logDebug("UpdateCenter", msg); }

    public static void logDebug(final String tag, final String msg) {
        if (IS_LOG_DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static DatabaseHandler getDb() {
        return DatabaseHandler.getInstance(Application.sApplicationContext);
    }

    public static DownloadManager getDownloadManager() { return downloadManager; }
}
