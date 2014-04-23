package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;
import com.google.gson.annotations.SerializedName;

import org.namelessrom.updatecenter.R;

public class AppCard extends Card {

    @SerializedName("app_id")
    private final String mAppId;

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

    public AppCard(final String appId, final String title, final String description,
            final String versionName, final int versionCode, final String timestamp) {
        super(title, description, "#000", "#000", false, true);
        this.mAppId = appId;
        this.mTitle = title;
        this.mDescription = description;
        this.mVersionName = versionName;
        this.mVersionCode = versionCode;
        this.mTimestamp = timestamp;
    }

    @Override
    public View getCardContent(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.card_app, null, false);

        ((TextView) v.findViewById(R.id.title)).setText(titlePlay);
        ((TextView) v.findViewById(R.id.title)).setTextColor(Color
                .parseColor(titleColor));
        ((TextView) v.findViewById(R.id.description)).setText(description);
        v.findViewById(R.id.stripe).setBackgroundColor(Color.parseColor(color));

        if (isClickable) {
            v.findViewById(R.id.contentLayout).setBackgroundResource(
                    R.drawable.selectable_background_cardbank);
        }

        if (hasOverflow) {
            v.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.overflow).setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public boolean convert(View convertCardView) {
        return false;
    }

}
