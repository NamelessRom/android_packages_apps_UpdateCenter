package org.namelessrom.updatecenter.items;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alex on 23.04.14.
 */
public class JsonUpdateInfo {

    @SerializedName("channel")
    private String mChannel;

    @SerializedName("filename")
    private String mFilename;

    @SerializedName("md5sum")
    private String mMd5;

    @SerializedName("downloadurl")
    private String mDownloadUrl;

    @SerializedName("timestamp")
    private String mTimestamp;

    @SerializedName("changelog")
    private String mChangeLog;

    public JsonUpdateInfo(final String channel, final String filename, final String md5,
            final String downloadUrl, final String timestamp, final String changelog) {
        this.mChannel = channel;
        this.mFilename = filename;
        this.mMd5 = md5;
        this.mDownloadUrl = downloadUrl;
        this.mTimestamp = timestamp;
        this.mChangeLog = changelog;
    }

    public String getChannel() { return mChannel; }

    public String getFilename() { return mFilename; }

    public String getMd5() { return mMd5; }

    public String getDownloadUrl() { return mDownloadUrl; }

    public String getTimestamp() { return mTimestamp; }

    public String getChangeLog() { return mChangeLog; }
}
