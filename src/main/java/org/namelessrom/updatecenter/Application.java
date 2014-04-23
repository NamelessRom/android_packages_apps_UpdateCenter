package org.namelessrom.updatecenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.net.OkHttpStack;
import org.namelessrom.updatecenter.net.images.ImageCacheManager;
import org.namelessrom.updatecenter.utils.Helper;

import java.util.List;

/**
 * Created by alex on 17.02.14.
 */
public class Application extends android.app.Application {

    private static final String TAG = Application.class.getName();

    public static final Handler sHandler = new Handler();

    public static boolean IS_DEBUG     = false;
    public static boolean IS_LOG_DEBUG = false;

    // TODO: update every time the supported api version changes
    public static final String API_CLIENT = "2.1.1";

    private static Context sApplicationContext;

    public static List<DownloadItem> mDownloadItems;

    private static int                   DISK_IMAGECACHE_SIZE            = 1024 * 1024 * 10;
    private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT =
            Bitmap.CompressFormat.PNG;
    private static int                   DISK_IMAGECACHE_QUALITY         = 75;

    private static RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplicationContext = this;

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

        logDebug("Debugging enabled!");

        createImageCache();

        mRequestQueue = Volley.newRequestQueue(sApplicationContext, new OkHttpStack());

        mDownloadItems = DatabaseHandler.getInstance(sApplicationContext).getAllItems(
                DatabaseHandler.TABLE_DOWNLOADS);

    }

    public static void logDebug(String msg) {
        if (IS_LOG_DEBUG) {
            Log.e("UpdateCenter", msg);
        }
    }

    /**
     * Create the image cache.
     */
    private void createImageCache() {
        ImageCacheManager.getInstance().init(this,
                this.getPackageCodePath(),
                DISK_IMAGECACHE_SIZE,
                DISK_IMAGECACHE_COMPRESS_FORMAT,
                DISK_IMAGECACHE_QUALITY);
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public static RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(sApplicationContext);
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public static <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public static <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public static void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
