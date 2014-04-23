package org.namelessrom.updatecenter.events;

/**
 * Created by alex on 22.04.14.
 */
public class IdEvent {

    public static final int ID_APP_LIST_LOADED = 0;

    private final int mId;

    public IdEvent(final int id) { mId = id; }

    public int getId() { return mId; }

}
