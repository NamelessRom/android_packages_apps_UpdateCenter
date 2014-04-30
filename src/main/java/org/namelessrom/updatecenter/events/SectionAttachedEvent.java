package org.namelessrom.updatecenter.events;

public class SectionAttachedEvent {

    private final int mId;

    public SectionAttachedEvent(final int id) { mId = id; }

    public int getId() { return mId; }

}
