package org.namelessrom.updatecenter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.utils.Helper;

import java.util.List;

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formKey = "",
        formUri = "https://reports.nameless-rom.org" +
                "/acra-namelesscenter/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "namelessreporter",
        formUriBasicAuthPassword = "weareopentoeveryone",
        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogOkToast = R.string.crash_dialog_ok_toast)
public class Application extends android.app.Application {

    private static final String TAG = Application.class.getName();

    public static final Handler sHandler = new Handler();

    public static final boolean IS_DEBUG     = false;
    public static       boolean IS_LOG_DEBUG = true;

    // TODO: update every time the supported api version changes
    public static final String API_CLIENT = "2.1.2";

    public static Context        sApplicationContext;
    public static PackageManager packageManager;

    public static List<DownloadItem> mDownloadItems;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        sApplicationContext = this;
        packageManager = sApplicationContext.getPackageManager();

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

        mDownloadItems = DatabaseHandler.getInstance(sApplicationContext).getAllItems(
                DatabaseHandler.TABLE_DOWNLOADS);
    }

    public static void logDebug(String msg) {
        logDebug("UpdateCenter", msg);
    }

    public static void logDebug(final String tag, final String msg) {
        if (IS_LOG_DEBUG) {
            Log.e(tag, msg);
        }
    }

}
