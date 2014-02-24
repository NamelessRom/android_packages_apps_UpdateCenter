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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by alex on 06.01.14.
 */
public class UpdateInfo implements Parcelable, Serializable {

    private static final long serialVersionUID = 5499890003569313403L;

    private String mUpdateChannel;
    private String mUpdateChannelShort;
    private String mUpdateName;
    private String mUpdateMd5;
    private String mUpdateUrl;
    private String mUpdateTimeStamp;
    private String mUpdateChangeLog;

    public UpdateInfo(String updateChannel, String updateName) {
        this(updateChannel, updateName, "-", "-", "-", "-");
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5) {
        this(updateChannel, updateName, updateMd5, "-", "-", "-");
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5,
                      String updateUrl) {
        this(updateChannel, updateName, updateMd5, updateUrl, "-", "-");
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5,
                      String updateUrl, String updateTimeStamp) {
        this(updateChannel, updateName, updateMd5, updateUrl, updateTimeStamp, "-");
    }

    private UpdateInfo(Parcel in) {
        readFromParcel(in);
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5,
                      String updateUrl, String updateTimeStamp, String updateChangeLog) {
        mUpdateChannel = updateChannel;
        mUpdateName = updateName;
        mUpdateMd5 = updateMd5;
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

    public String getUpdateMd5() {
        return mUpdateMd5;
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

    @Override
    public String toString() {
        return "UpdateInfo: " + mUpdateName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof UpdateInfo)) {
            return false;
        }

        UpdateInfo ui = (UpdateInfo) o;
        return TextUtils.equals(mUpdateChannel, ui.mUpdateChannel)
                && TextUtils.equals(mUpdateChannelShort, ui.mUpdateChannelShort)
                && TextUtils.equals(mUpdateName, ui.mUpdateName)
                && TextUtils.equals(mUpdateMd5, ui.mUpdateMd5)
                && TextUtils.equals(mUpdateUrl, ui.mUpdateUrl)
                && TextUtils.equals(mUpdateTimeStamp, ui.mUpdateTimeStamp)
                && TextUtils.equals(mUpdateChangeLog, ui.mUpdateChangeLog);
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mUpdateChannel);
        parcel.writeString(mUpdateChannelShort);
        parcel.writeString(mUpdateName);
        parcel.writeString(mUpdateMd5);
        parcel.writeString(mUpdateUrl);
        parcel.writeString(mUpdateTimeStamp);
        parcel.writeString(mUpdateChangeLog);
    }

    private void readFromParcel(Parcel in) {
        mUpdateChannel = in.readString();
        mUpdateChannelShort = in.readString();
        mUpdateName = in.readString();
        mUpdateMd5 = in.readString();
        mUpdateUrl = in.readString();
        mUpdateTimeStamp = in.readString();
        mUpdateChangeLog = in.readString();
    }
}
