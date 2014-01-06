/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.updatecenter.utils.items;

/**
 * Created by alex on 06.01.14.
 */
public class UpdateListItem {

    private final String mUpdateChannel;
    private final String mUpdateChannelShort;
    private final String mUpdateName;
    private final String mUpdateInfo;
    private final String mUpdateUrl;
    private final String mUpdateTimeStamp;
    private final String mUpdateChangeLog;

    public UpdateListItem(String updateChannel, String updateName) {
        this(updateChannel, updateName, "-", "-", "-", "-");
    }

    public UpdateListItem(String updateChannel, String updateName, String updateInfo) {
        this(updateChannel, updateName, updateInfo, "-", "-", "-");
    }

    public UpdateListItem(String updateChannel, String updateName, String updateInfo,
                          String updateUrl) {
        this(updateChannel, updateName, updateInfo, updateUrl, "-", "-");
    }

    public UpdateListItem(String updateChannel, String updateName, String updateInfo,
                          String updateUrl, String updateTimeStamp) {
        this(updateChannel, updateName, updateInfo, updateUrl, updateTimeStamp, "-");
    }

    public UpdateListItem(String updateChannel, String updateName, String updateInfo,
                          String updateUrl, String updateTimeStamp, String updateChangeLog) {
        mUpdateChannel = updateChannel;
        mUpdateName = updateName;
        mUpdateInfo = updateInfo;
        mUpdateUrl = updateUrl;
        mUpdateTimeStamp = updateTimeStamp;
        mUpdateChangeLog = updateChangeLog;

        if (mUpdateChannel.equals("NIGHTLY")) {
            mUpdateChannelShort = "N";
        } else if (mUpdateChannel.equals("MILESTONE")) {
            mUpdateChannelShort = "M";
        } else if (mUpdateChannel.equals("RELEASECANDIDATE")) {
            mUpdateChannelShort = "RC";
        } else if (mUpdateChannel.equals("STABLE")) {
            mUpdateChannelShort = "S";
        } else if (mUpdateChannel.equals("---")) {
            mUpdateChannelShort = "";
        } else {
            mUpdateChannelShort = "?";
        }
    }

    public String getUpdateChannel() {
        return mUpdateChannel;
    }

    public String getUpdateChannelShort() {
        return mUpdateChannelShort;
    }

    public String getUpdateName() {
        return mUpdateName;
    }

    public String getUpdateInfo() {
        return mUpdateInfo;
    }

    public String getUpdateUrl() {
        return mUpdateUrl;
    }

    public String getUpdateTimeStamp() {
        return mUpdateTimeStamp;
    }

    public String getUpdateChangeLog() {
        return mUpdateChangeLog;
    }

}
