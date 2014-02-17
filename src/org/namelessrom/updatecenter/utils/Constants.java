package org.namelessrom.updatecenter.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by alex on 17.02.14.
 */
public interface Constants {

    public static final String SD_ROOT_DIR = Environment.getExternalStorageDirectory()
            + File.separator + "Nameless/UpdateCenter";

}
