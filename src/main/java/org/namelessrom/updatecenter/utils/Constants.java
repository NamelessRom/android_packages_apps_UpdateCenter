package org.namelessrom.updatecenter.utils;

import android.os.Environment;

import org.namelessrom.updatecenter.R;

import java.io.File;

/**
 * Created by alex on 17.02.14.
 */
public interface Constants {

    public static final boolean LOCALHOST_TESTING = false; // TODO: set to false
    //==============================================================================================
    // MENU
    //==============================================================================================
    public static final int[]   MENU_ICONS        = {
            R.drawable.ic_menu_home,
            -1,
            R.drawable.ic_menu_get_apps,
            -1,
            R.drawable.ic_menu_update,
            -1,
            R.drawable.ic_menu_preferences
    };

    //==============================================================================================
    // ID
    //==============================================================================================
    /*public static final int ID_DUMMY                    = -5;*/
    public static final int ID_FIRST_MENU             = -4;
    public static final int ID_SECOND_MENU            = -3;
    public static final int ID_RESTORE                = -2;
    public static final int ID_RESTORE_FROM_SUB       = -1;
    //----------------------------------------------------------------------------------------------
    public static final int ID_UC                     = 0;
    public static final int ID_APP_LIST               = 2;
    public static final int ID_APP_DETAILS            = ID_APP_LIST + 1000;
    public static final int ID_ROM_UPDATE             = 4;
    public static final int ID_ROM_UPDATE_PREFERENCES = ID_ROM_UPDATE + 1000;
    public static final int ID_PREFERENCES            = 6;

    //==============================================================================================
    // PATHS
    //==============================================================================================
    public static final String SD_ROOT_DIR              =
            Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String UPDATE_FOLDER            = "Nameless/UpdateCenter";
    public static final String UPDATE_FOLDER_CHANGELOG  =
            SD_ROOT_DIR + File.separator + UPDATE_FOLDER + File.separator + "Changelogs";
    public static final String UPDATE_FOLDER_FULL       =
            SD_ROOT_DIR + File.separator + UPDATE_FOLDER;
    public static final String UPDATE_FOLDER_ADDITIONAL = UPDATE_FOLDER_FULL + "/FlashAfterUpdate";
    public static final String DOWNLOAD_ID              = "download_id";

    //==============================================================================================
    // URLS
    //==============================================================================================
    public static final String URL             = LOCALHOST_TESTING
            ? "http://192.168.43.234:3000"
            : "https://api.nameless-rom.org";
    public static final String API_URL         = URL + "/version";
    public static final String ROM_URL         = URL + "/update";
    //----------------------------------------------------------------------------------------------
    public static final String APP_URL         = URL + "/app";
    public static final String APP_URL_QUERY   = "?skip=%s&count=10";
    public static final String APP_COUNT_URL   = APP_URL + "/count";
    public static final String APP_DETAILS_URL = APP_URL + "/%s";
    public static final String APP_PACKAGE_URL = APP_DETAILS_URL + "/app";
    public static final String APP_IMAGE_URL   = APP_DETAILS_URL + "/icon";
    //----------------------------------------------------------------------------------------------
    public static final String UC_APK          = URL + "/uc.apk";
    public static final String UC_APK_VERSION  = URL + "/uc.apk.version";
    //----------------------------------------------------------------------------------------------
    public static final String CHANNEL_NIGHTLY = "NIGHTLY";

    //==============================================================================================
    // JSON NODE NAMES
    //==============================================================================================
    public static final String TAG_CHANNEL     = "channel";
    public static final String TAG_FILENAME    = "filename";
    public static final String TAG_MD5SUM      = "md5sum";
    public static final String TAG_TIMESTAMP   = "timestamp";
    public static final String TAG_CHANGELOG   = "changelog";
    public static final String TAG_URL         = "downloadurl";
    //----------------------------------------------------------------------------------------------
    public static final String TAG_API_VERSION = "api_version";

    //==============================================================================================
    // PREFERENCES
    //==============================================================================================
    public static final String UPDATE_CHECK_PREF           = "pref_update_check_interval";
    public static final String UPDATE_TYPE_PREF            = "pref_update_types";
    public static final String LAST_UPDATE_CHECK_PREF      = "pref_last_update_check";
    public static final String LAST_AUTO_UPDATE_CHECK_PREF = "pref_last_auto_update_check";
    //----------------------------------------------------------------------------------------------
    public static final String PREF_RECOVERY_TYPE          = "pref_recovery_type";
    public static final int    RECOVERY_TYPE_BOTH          = 0;
    public static final int    RECOVERY_TYPE_CWM           = 1;
    public static final int    RECOVERY_TYPE_OPEN          = 2;
    //----------------------------------------------------------------------------------------------
    public static final String BOOT_CHECK_COMPLETED        = "boot_check_completed";
    public static final int    UPDATE_FREQ_NONE            = -1;
    public static final int    UPDATE_FREQ_AT_APP_START    = -2;
    public static final int    UPDATE_FREQ_AT_BOOT         = -3;
    public static final int    UPDATE_FREQ_TWICE_DAILY     = 43200;
    public static final int    UPDATE_FREQ_DAILY           = 86400;
    public static final int    UPDATE_FREQ_WEEKLY          = 604800;
    public static final int    UPDATE_FREQ_BI_WEEKLY       = 1209600;
    public static final int    UPDATE_FREQ_MONTHLY         = 2419200;
}
