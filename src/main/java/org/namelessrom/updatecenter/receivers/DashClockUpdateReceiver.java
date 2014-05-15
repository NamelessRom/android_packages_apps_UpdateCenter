package org.namelessrom.updatecenter.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.services.dashclock.RomUpdateDashclockExtension;

import java.util.ArrayList;

/**
 * Created by alex on 15.05.14.
 */
public class DashClockUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        final ArrayList<UpdateInfo> updates = intent.getParcelableArrayListExtra(
                RomUpdateDashclockExtension.EXTRA_UPDATES);

        final Intent i = new Intent(context, RomUpdateDashclockExtension.class);
        i.setAction(RomUpdateDashclockExtension.ACTION_DATA_UPDATE);
        i.putParcelableArrayListExtra(RomUpdateDashclockExtension.EXTRA_UPDATES, updates);
        context.startService(i);
    }
}
