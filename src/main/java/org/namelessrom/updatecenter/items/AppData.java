package org.namelessrom.updatecenter.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by alex on 24.04.14.
 */
public class AppData implements Serializable {
    @SerializedName("app_id")
    private final String mAppId;

    @SerializedName("developer")
    private final String mDeveloper;

    @SerializedName("title")
    private final String mTitle;

    @SerializedName("description")
    private final String mDescription;

    @SerializedName("versionname")
    private final String mVersionName;

    @SerializedName("versioncode")
    private final int mVersionCode;

    @SerializedName("timestamp")
    private final String mTimestamp;

    public AppData(final String appId, final String developer, final String title,
            final String description, final String versionName, final int versionCode,
            final String timestamp) {
        this.mAppId = appId;
        this.mDeveloper = developer;
        this.mTitle = title;
        this.mDescription = description;
        this.mVersionName = versionName;
        this.mVersionCode = versionCode;
        this.mTimestamp = timestamp;
    }

    public String getAppId() { return mAppId; }

    public String getDeveloper() { return mDeveloper; }

    public String getTitle() { return mTitle; }

    public String getDescription() { return mDescription; }

    public String getVersionName() { return mVersionName; }

    public int getVersionCode() { return mVersionCode; }

    public String getTimestamp() { return mTimestamp; }

}
