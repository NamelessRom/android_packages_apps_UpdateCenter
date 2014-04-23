package org.namelessrom.updatecenter.utils;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {

    private static final Bus sBus = new Bus(ThreadEnforcer.ANY);

    private BusProvider() { /* ignored */ }

    /**
     * WARNING! ANY THREAD ENFORCER!
     * Be sure to run your subscribed methods on the main thread!
     *
     * @return the one and only bus
     */
    public static Bus getBus() {
        return sBus;
    }

}
