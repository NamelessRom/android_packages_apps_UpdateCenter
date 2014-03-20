package org.namelessrom.updatecenter.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.net.HttpHandler;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alex on 24.02.14.
 */
public class UpdateCheckService extends IntentService implements Constants {

    private static final String TAG = "UpdateCheckService";

    // request actions
    public static final String ACTION_CHECK        = "org.namelessrom.updatecenter.action.CHECK";
    public static final String ACTION_CANCEL_CHECK =
            "org.namelessrom.updatecenter.action.CANCEL_CHECK";

    // broadcast actions
    public static final String ACTION_CHECK_FINISHED =
            "org.namelessrom.updatecenter.action.UPDATE_CHECK_FINISHED";
    // extra for ACTION_CHECK_FINISHED: total amount of found updates
    public static final String EXTRA_UPDATE_COUNT    = "update_count";

    // max. number of updates listed in the expanded notification
    private static final int EXPANDED_NOTIF_UPDATE_COUNT = 4;

    public UpdateCheckService() {
        super("UpdateCheckService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (TextUtils.equals(intent.getAction(), ACTION_CANCEL_CHECK)) {
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Helper.isOnline(this)) {
            // Only check for updates if the device is actually connected to a network
            Log.i(TAG, "Could not check for updates. Not connected to the network.");
            return;
        }

        // Start the update check
        Intent finishedIntent = new Intent(ACTION_CHECK_FINISHED); // for dashclock
        List<UpdateInfo> availableUpdates;
        try {
            availableUpdates = getAvailableUpdatesAndFillIntent(finishedIntent);
        } catch (IOException e) {
            Log.e(TAG, "Could not check for updates", e);
            availableUpdates = null;
        }

        if (availableUpdates == null) {
            sendBroadcast(finishedIntent);
            return;
        }

        // Store the last update check time and ensure boot check completed is true
        final Date d = new Date();
        PreferenceManager.getDefaultSharedPreferences(UpdateCheckService.this).edit()
                .putLong(LAST_UPDATE_CHECK_PREF, d.getTime())
                .putBoolean(BOOT_CHECK_COMPLETED, true)
                .apply();

        int realUpdateCount = finishedIntent.getIntExtra(EXTRA_UPDATE_COUNT, 0);

        // Write to log
        Log.i(TAG, "The update check successfully completed at " + d + " and found "
                + availableUpdates.size() + " updates ("
                + realUpdateCount + " newer than installed)");

        if (realUpdateCount != 0) {
            // There are updates available
            // The notification should launch the main app
            Intent i = new Intent(this, MainActivity.class);
            // TODO: give extra to launch in updates
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
                    PendingIntent.FLAG_ONE_SHOT);

            Resources res = getResources();
            String text = res.getQuantityString(R.plurals.not_new_updates_found_body,
                    realUpdateCount, realUpdateCount);

            // Get the notification ready
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(res.getString(R.string.not_new_updates_found_ticker))
                    .setContentTitle(res.getString(R.string.not_new_updates_found_title))
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            Notification.InboxStyle inbox = new Notification.InboxStyle(builder)
                    .setBigContentTitle(text);
            int added = 0, count = availableUpdates.size();

            for (UpdateInfo ui : availableUpdates) {
                if (added < EXPANDED_NOTIF_UPDATE_COUNT) {
                    inbox.addLine(ui.getUpdateName());
                    added++;
                }
            }
            if (added != count) {
                inbox.setSummaryText(res.getQuantityString(R.plurals.not_additional_count,
                        count - added, count - added));
            }
            builder.setStyle(inbox);
            builder.setNumber(availableUpdates.size());

            if (count == 1) {
                final UpdateInfo updateInfo = availableUpdates.get(0);

                if (!new File(UPDATE_FOLDER_FULL + File.separator
                        + updateInfo.getUpdateName() + ".zip").exists()) {
                    i = new Intent(this, DownloadReceiver.class);
                    i.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
                    i.putExtra(DownloadReceiver.EXTRA_UPDATE_INFO, (Parcelable) updateInfo);
                    final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i,
                            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.addAction(R.drawable.ic_tab_download,
                            res.getString(R.string.not_action_download), pendingIntent);
                }
            }

            // Trigger the notification
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(R.string.not_new_updates_found_title, builder.build());
        }

        sendBroadcast(finishedIntent);
    }

    private List<UpdateInfo> getAvailableUpdatesAndFillIntent(Intent intent) throws IOException {
        // Get the type of update we should check for
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // int updateType = prefs.getInt(Constants.UPDATE_TYPE_PREF, 0); // TODO: choose channel

        List<UpdateInfo> updates = new ArrayList<UpdateInfo>();

        // Builds: BaseUrl + Channel + DeviceId
        final String url = ROM_URL + "/" + CHANNEL_NIGHTLY + "/"
                + Helper.readBuildProp("ro.nameless.device");

        int currentDate;

        try {
            currentDate = Integer.parseInt(Helper.readBuildProp("ro.nameless.date"));
        } catch (Exception exc) {
            currentDate = 20140101;
        }
        //Log.e("HttpHandler", "Url: " + url);

        String jsonStr = HttpHandler.get(url);

        if (jsonStr != null) {
            try {
                // Getting JSON Array node
                JSONArray mUpdateArray = new JSONArray(jsonStr);

                if (mUpdateArray.length() == 0) {
                    return null;
                }

                for (int i = 0; i < mUpdateArray.length(); i++) {
                    JSONObject c = mUpdateArray.getJSONObject(i);

                    //final String id = c.getString(TAG_ID);
                    final String channel = c.getString(TAG_CHANNEL);
                    final String filename = c.getString(TAG_FILENAME).replace(".zip", "");
                    final String md5sum = c.getString(TAG_MD5SUM);
                    final String urlFile = c.getString(TAG_URL);
                    final String timeStampString = c.getString(TAG_TIMESTAMP);
                    int timeStamp;
                    try {
                        timeStamp = Integer.parseInt(timeStampString);
                    } catch (Exception exc) {
                        timeStamp = 20140102;
                    }
                    final String changeLog = c.getString(TAG_CHANGELOG);

                    if (currentDate < timeStamp) {
                        UpdateInfo item = new UpdateInfo(channel, filename, md5sum,
                                urlFile, timeStampString, changeLog);

                        updates.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HttpHandler", "Couldn't get any data from the url");
        }

        intent.putExtra(EXTRA_UPDATE_COUNT, updates.size());

        return updates;
    }
}
