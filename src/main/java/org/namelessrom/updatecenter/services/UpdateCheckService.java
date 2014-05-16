package org.namelessrom.updatecenter.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.activities.MainActivity;
import org.namelessrom.updatecenter.events.UpdateCheckDoneEvent;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.receivers.DownloadReceiver;
import org.namelessrom.updatecenter.services.dashclock.RomUpdateDashclockExtension;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UpdateCheckService extends Service implements Constants {

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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        mAction = intent.getAction();

        if (TextUtils.equals(mAction, ACTION_CANCEL_CHECK)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!Helper.isOnline(this)) {
            // Only check for updates if the device is actually connected to a network
            postBus(new UpdateCheckDoneEvent(false));
            return START_NOT_STICKY;
        }

        final String url =
                ROM_URL + "/" + CHANNEL_NIGHTLY + "/" + Helper.readBuildProp("ro.nameless.device");

        Ion.with(this).load(url).as(UpdateInfo[].class).setCallback(mCallBack);

        return super.onStartCommand(intent, flags, startId);
    }

    final FutureCallback<UpdateInfo[]> mCallBack = new FutureCallback<UpdateInfo[]>() {
        @Override
        public void onCompleted(Exception e, UpdateInfo[] result) {
            if (result == null || e != null) {
                postBus(new UpdateCheckDoneEvent(false));
                return;
            }

            final List<UpdateInfo> list = Arrays.asList(result);
            final ArrayList<UpdateInfo> updates = new ArrayList<UpdateInfo>();
            final int currentDate = Helper.getBuildDate();

            for (final UpdateInfo info : list) {
                final String channel = info.getChannel();
                final String filename = info.getName().replace(".zip", "");
                final String md5sum = info.getMd5();
                final String urlFile = info.getUrl();
                final String timeStampString = info.getTimestamp();
                final int timeStamp = Helper.parseDate(timeStampString);

                if (currentDate < timeStamp) {
                    final UpdateInfo item = new UpdateInfo(channel, filename, md5sum,
                            urlFile, timeStampString);

                    updates.add(item);
                }
            }

            final Intent updateIntent =
                    new Intent(RomUpdateDashclockExtension.ACTION_DATA_UPDATE);
            updateIntent.putParcelableArrayListExtra(RomUpdateDashclockExtension.EXTRA_UPDATES,
                    updates);
            sendBroadcast(updateIntent);

            if (ACTION_CHECK_UI.equals(mAction)) {
                postBus(new UpdateCheckDoneEvent(true, updates));
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
                Intent i = new Intent(UpdateCheckService.this, MainActivity.class);
                // TODO: give extra to launch in updates
                final PendingIntent contentIntent = PendingIntent.getActivity(
                        UpdateCheckService.this, 0, i, PendingIntent.FLAG_ONE_SHOT);

                final Resources res = getResources();
                final String text = res.getQuantityString(R.plurals.not_new_updates_found_body,
                        realUpdateCount, realUpdateCount);

                // Get the notification ready
                Notification.Builder builder = new Notification.Builder(UpdateCheckService.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(res.getString(R.string.new_updates_found_ticker))
                        .setContentTitle(res.getString(R.string.new_updates_found_title))
                        .setContentText(text)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

                Notification.InboxStyle inbox = new Notification.InboxStyle(builder)
                        .setBigContentTitle(text);
                int added = 0;

                for (final UpdateInfo ui : updates) {
                    if (added < EXPANDED_NOTIF_UPDATE_COUNT) {
                        inbox.addLine(ui.getName());
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
                            + updateInfo.getName() + ".zip").exists()) {
                        i = new Intent(UpdateCheckService.this, DownloadReceiver.class);
                        i.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
                        i.putExtra(DownloadReceiver.EXTRA_UPDATE_INFO, (Parcelable) updateInfo);
                        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                UpdateCheckService.this, 0, i,
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
                        builder.addAction(R.drawable.ic_stat_notify_download,
                                res.getString(R.string.download), pendingIntent);
                    }
                }

                // Trigger the notification
                NotificationManager nm =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(R.string.new_updates_found_title, builder.build());
            }

            sendBroadcast(finishedIntent);
            postBus(new UpdateCheckDoneEvent(true, updates));
        }
    };

    private void postBus(final UpdateCheckDoneEvent event) {
        BusProvider.getBus().post(event);
        stopSelf();
    }
}
