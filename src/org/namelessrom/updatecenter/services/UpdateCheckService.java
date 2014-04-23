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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.events.UpdateCheckDoneEvent;
import org.namelessrom.updatecenter.net.requests.GsonRequest;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.items.JsonUpdateInfo;
import org.namelessrom.updatecenter.utils.items.UpdateInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.namelessrom.updatecenter.Application.logDebug;

public class UpdateCheckService extends IntentService implements Constants,
        Response.Listener<JsonUpdateInfo[]>, Response.ErrorListener {

    // request actions
    public static final String ACTION_CHECK        = "org.namelessrom.updatecenter.action.CHECK";
    public static final String ACTION_CANCEL_CHECK =
            "org.namelessrom.updatecenter.action.CANCEL_CHECK";
    public static final String ACTION_CHECK_UI     = "org.namelessrom.updatecenter.action.CHECK_UI";

    // broadcast actions
    public static final String ACTION_CHECK_FINISHED =
            "org.namelessrom.updatecenter.action.UPDATE_CHECK_FINISHED";
    // extra for ACTION_CHECK_FINISHED: total amount of found updates
    public static final String EXTRA_UPDATE_COUNT    = "update_count";

    // max. number of updates listed in the expanded notification
    private static final int EXPANDED_NOTIF_UPDATE_COUNT = 4;

    private String mAction;

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
    protected void onHandleIntent(final Intent intent) {
        if (intent == null) return;
        mAction = intent.getAction();

        if (!Helper.isOnline(this)) {
            // Only check for updates if the device is actually connected to a network
            BusProvider.getBus().post(new UpdateCheckDoneEvent(false));
            return;
        }

        final String url = ROM_URL + "/" + CHANNEL_NIGHTLY + "/"
                + Helper.readBuildProp("ro.nameless.device");

        Application.addToRequestQueue(new GsonRequest(Request.Method.GET, url,
                JsonUpdateInfo[].class, this, this));
    }

    @Override
    public void onErrorResponse(final VolleyError volleyError) {
        logDebug("Error when checking for updates: " + volleyError.getMessage());
        BusProvider.getBus().post(new UpdateCheckDoneEvent(false));
    }

    @Override
    public void onResponse(final JsonUpdateInfo[] jsonUpdates) {
        if (jsonUpdates == null) {
            BusProvider.getBus().post(new UpdateCheckDoneEvent(false));
            return;
        }

        final List<JsonUpdateInfo> list = Arrays.asList(jsonUpdates);
        final List<UpdateInfo> updates = new ArrayList<UpdateInfo>();
        final int currentDate = Helper.getBuildDate();

        for (final JsonUpdateInfo info : list) {
            final String channel = info.getChannel();
            final String filename = info.getFilename().replace(".zip", "");
            final String md5sum = info.getMd5();
            final String urlFile = info.getDownloadUrl();
            final String timeStampString = info.getTimestamp();
            final int timeStamp = Helper.parseDate(timeStampString);
            final String changeLog = info.getChangeLog();

            if (currentDate < timeStamp) {
                final UpdateInfo item = new UpdateInfo(channel, filename, md5sum,
                        urlFile, timeStampString, changeLog);

                updates.add(item);
            }
        }

        if (ACTION_CHECK_UI.equals(mAction)) {
            BusProvider.getBus().post(new UpdateCheckDoneEvent(true, updates));
            return;
        }

        final int realUpdateCount = updates.size();

        final Intent finishedIntent = new Intent(ACTION_CHECK_FINISHED); // for dashclock
        finishedIntent.putExtra(EXTRA_UPDATE_COUNT, realUpdateCount);

        // Store the last update check time and ensure boot check completed is true
        final Date d = new Date();
        PreferenceManager.getDefaultSharedPreferences(UpdateCheckService.this).edit()
                .putLong(LAST_UPDATE_CHECK_PREF, d.getTime())
                .putBoolean(BOOT_CHECK_COMPLETED, true)
                .apply();

        if (realUpdateCount != 0) {
            // There are updates available
            // The notification should launch the main app
            Intent i = new Intent(this, MainActivity.class);
            // TODO: give extra to launch in updates
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
                    PendingIntent.FLAG_ONE_SHOT);

            final Resources res = getResources();
            final String text = res.getQuantityString(R.plurals.not_new_updates_found_body,
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
            int added = 0;

            for (final UpdateInfo ui : updates) {
                if (added < EXPANDED_NOTIF_UPDATE_COUNT) {
                    inbox.addLine(ui.getUpdateName());
                    added++;
                }
            }
            if (added != realUpdateCount) {
                inbox.setSummaryText(res.getQuantityString(R.plurals.not_additional_count,
                        realUpdateCount - added, realUpdateCount - added));
            }
            builder.setStyle(inbox);
            builder.setNumber(realUpdateCount);

            if (realUpdateCount == 1) {
                final UpdateInfo updateInfo = updates.get(0);

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
        BusProvider.getBus().post(new UpdateCheckDoneEvent(true, updates));
    }
}
