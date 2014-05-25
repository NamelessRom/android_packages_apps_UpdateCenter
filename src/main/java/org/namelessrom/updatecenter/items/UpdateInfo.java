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

package org.namelessrom.updatecenter.items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by alex on 06.01.14.
 */
public class UpdateInfo implements Parcelable, Serializable {

    public static final int CHANNEL_UNKNOWN   = -2;
    public static final int CHANNEL_EMPTY     = -1;
    public static final int CHANNEL_NIGHTLY   = 1;
    public static final int CHANNEL_WEEKLY    = 2;
    public static final int CHANNEL_MILESTONE = 3;
    public static final int CHANNEL_RC        = 4;
    public static final int CHANNEL_STABLE    = 5;

    private static final long serialVersionUID = 5499890003569313403L;

    @SerializedName("channel")
    private String  mChannel;
    private String  mChannelShort;
    private int     mChannelType;
    @SerializedName("filename")
    private String  mName;
    @SerializedName("md5sum")
    private String  mMd5;
    @SerializedName("downloadurl")
    private String  mUrl;
    @SerializedName("timestamp")
    private String  mTimestamp;
    private boolean mIsDownloading;

    private UpdateInfo(final Parcel in) { readFromParcel(in); }

    public UpdateInfo(String updateChannel, String updateName) {
        this(updateChannel, updateName, "-", "-", "-");
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5) {
        this(updateChannel, updateName, updateMd5, "-", "-");
    }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5,
            String updateUrl) { this(updateChannel, updateName, updateMd5, updateUrl, "-"); }

    public UpdateInfo(String updateChannel, String updateName, String updateMd5,
            String updateUrl, String updateTimeStamp) {
        mChannel = updateChannel;
        mName = updateName;
        mMd5 = updateMd5;
        mUrl = updateUrl;
        mTimestamp = updateTimeStamp;

        if (mChannel.equals("NIGHTLY")) {
            mChannelShort = "N";
            mChannelType = CHANNEL_NIGHTLY;
        } else if (mChannel.equals("WEEKLY")) {
            mChannelShort = "W";
            mChannelType = CHANNEL_WEEKLY;
        } else if (mChannel.equals("MILESTONE")) {
            mChannelShort = "M";
            mChannelType = CHANNEL_MILESTONE;
        } else if (mChannel.equals("RELEASECANDIDATE")) {
            mChannelShort = "RC";
            mChannelType = CHANNEL_RC;
        } else if (mChannel.equals("STABLE")) {
            mChannelShort = "S";
            mChannelType = CHANNEL_STABLE;
        } else if (mChannel.equals("---")) {
            mChannelShort = "";
            mChannelType = CHANNEL_EMPTY;
        } else if (!mChannel.isEmpty()) {
            mChannelShort = mChannel.substring(0, 1);
            mChannelType = CHANNEL_UNKNOWN;
        } else {
            mChannelShort = "?";
            mChannelType = CHANNEL_UNKNOWN;
        }
    }

    public String getChannel() { return mChannel; }

    public String getChannelShort() { return mChannelShort; }

    public String getName() { return mName; }

    public String getMd5() { return mMd5; }

    public String getUrl() { return mUrl; }

    public String getTimestamp() { return mTimestamp; }

    public boolean isDownloading() { return mIsDownloading; }

    public UpdateInfo setDownloading(boolean isDownloading) {
        mIsDownloading = isDownloading;
        return this;
    }

    public int getChannelType() { return mChannelType; }

    @Override
    public String toString() { return "UpdateInfo: " + mName; }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof UpdateInfo)) {
            return false;
        }

        UpdateInfo ui = (UpdateInfo) o;
        return TextUtils.equals(mChannel, ui.mChannel)
                && TextUtils.equals(mChannelShort, ui.mChannelShort)
                && TextUtils.equals(mName, ui.mName)
                && TextUtils.equals(mMd5, ui.mMd5)
                && TextUtils.equals(mUrl, ui.mUrl)
                && TextUtils.equals(mTimestamp, ui.mTimestamp)
                && mChannelType == ui.mChannelType
                && mIsDownloading == ui.isDownloading();
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR =
            new Parcelable.Creator<UpdateInfo>() {
                public UpdateInfo createFromParcel(Parcel in) { return new UpdateInfo(in); }

                public UpdateInfo[] newArray(int size) { return new UpdateInfo[size]; }
            };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mChannel);
        parcel.writeString(mChannelShort);
        parcel.writeString(mName);
        parcel.writeString(mMd5);
        parcel.writeString(mUrl);
        parcel.writeString(mTimestamp);
        parcel.writeString(mIsDownloading ? "1" : "0");
    }

    private void readFromParcel(Parcel in) {
        mChannel = in.readString();
        mChannelShort = in.readString();
        mName = in.readString();
        mMd5 = in.readString();
        mUrl = in.readString();
        mTimestamp = in.readString();
        mIsDownloading = in.readString().equals("1");
    }
}
