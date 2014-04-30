package org.namelessrom.updatecenter.items;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alex on 23.04.14.
 */
public class JsonUpdateInfo {

    @SerializedName("channel")
    private final String mChannel;

    @SerializedName("filename")
    private final String mFilename;

    @SerializedName("md5sum")
    private final String mMd5;

    @SerializedName("downloadurl")
    private final String mDownloadUrl;

    @SerializedName("timestamp")
    private final String mTimestamp;

    public JsonUpdateInfo(final String channel, final String filename, final String md5,
            final String downloadUrl, final String timestamp) {
        this.mChannel = channel;
        this.mFilename = filename;
        this.mMd5 = md5;
        this.mDownloadUrl = downloadUrl;
        this.mTimestamp = timestamp;
    }

    public String getChannel() { return mChannel; }

    public String getFilename() { return mFilename; }

    public String getMd5() { return mMd5; }

    public String getDownloadUrl() { return mDownloadUrl; }

    public String getTimestamp() { return mTimestamp; }
}
