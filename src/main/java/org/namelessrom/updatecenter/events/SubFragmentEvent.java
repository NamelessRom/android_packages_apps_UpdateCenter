package org.namelessrom.updatecenter.events;

import android.app.Fragment;

public class SubFragmentEvent {

    private final int      mId;
    private final Fragment mFragment;

    public SubFragmentEvent(final int id) { this(id, null); }

    public SubFragmentEvent(final int id, final Fragment fragment) {
        mId = id;
        mFragment = fragment;
    }

    public int getId() { return mId; }

    public Fragment getFragment() { return mFragment; }

}
