package org.namelessrom.updatecenter.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by alex on 17.02.14.
 */
public interface Constants {

    public static final String SD_ROOT_DIR              =
            Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String UPDATE_FOLDER            = "Nameless/UpdateCenter";
    public static final String UPDATE_FOLDER_FULL       =
            SD_ROOT_DIR + File.separator + UPDATE_FOLDER;
    public static final String UPDATE_FOLDER_ADDITIONAL = UPDATE_FOLDER_FULL + "/FlashAfterUpdate";
    public static final String DOWNLOAD_ID              = "download_id";

    public static final String URL             = "http://api.nameless-rom.org";
    public static final String ROM_URL         = URL + "/update";
    public static final String APP_URL         = URL + "/app";
    public static final String CHANNEL_NIGHTLY = "NIGHTLY";
    // JSON Node names
    public static final String TAG_CHANNEL     = "channel";
    public static final String TAG_FILENAME    = "filename";
    public static final String TAG_MD5SUM      = "md5sum";
    public static final String TAG_TIMESTAMP   = "timestamp";
    public static final String TAG_CHANGELOG   = "changelog";
    public static final String TAG_URL         = "downloadurl";

    // Preferences
    public static final String UPDATE_CHECK_PREF      = "pref_update_check_interval";
    public static final String UPDATE_TYPE_PREF       = "pref_update_types";
    public static final String LAST_UPDATE_CHECK_PREF = "pref_last_update_check";
    // Flasher
    public static final String PREF_RECOVERY_TYPE     = "pref_recovery_type";
    public static final int    RECOVERY_TYPE_BOTH     = 0;
    public static final int    RECOVERY_TYPE_CWM      = 1;
    public static final int    RECOVERY_TYPE_OPEN     = 2;

    // Update Check items
    public static final String BOOT_CHECK_COMPLETED     = "boot_check_completed";
    public static final int    UPDATE_FREQ_NONE         = -1;
    public static final int    UPDATE_FREQ_AT_APP_START = -2;
    public static final int    UPDATE_FREQ_AT_BOOT      = -3;
    public static final int    UPDATE_FREQ_TWICE_DAILY  = 43200;
    public static final int    UPDATE_FREQ_DAILY        = 86400;
    public static final int    UPDATE_FREQ_WEEKLY       = 604800;
    public static final int    UPDATE_FREQ_BI_WEEKLY    = 1209600;
    public static final int    UPDATE_FREQ_MONTHLY      = 2419200;
}
