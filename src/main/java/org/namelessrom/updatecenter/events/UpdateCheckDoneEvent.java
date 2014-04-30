package org.namelessrom.updatecenter.events;

import org.namelessrom.updatecenter.items.UpdateInfo;

import java.util.ArrayList;
import java.util.List;

public class UpdateCheckDoneEvent {

    private final boolean          mIsSuccess;
    private final List<UpdateInfo> mUpdates;


    public UpdateCheckDoneEvent(final boolean success) {
        this(success, new ArrayList<UpdateInfo>());
    }

    public UpdateCheckDoneEvent(final boolean success, final List<UpdateInfo> updates) {
        mIsSuccess = success;
        mUpdates = updates;
    }

    public boolean isSuccess() {
        return mIsSuccess;
    }

    public List<UpdateInfo> getUpdates() {
        return mUpdates;
    }

}
