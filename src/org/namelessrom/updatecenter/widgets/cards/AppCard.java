package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.fima.cardsui.objects.Card;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.net.images.ImageCacheManager;

public class AppCard extends Card {

    private final String mAppId;

    public AppCard(final String appId, final String title, final String description,
            final String versionName, final int versionCode, final String timestamp) {
        super(title, description, "#4ac925", "#669900", false, true);
        mAppId = appId;
    }

    public AppCard(final String appId, final String title, final String description) {
        super(title, description, "#4ac925", "#669900", false, true);
        mAppId = appId;
    }

    @Override
    public View getCardContent(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.card_app, null, false);

        ((TextView) v.findViewById(R.id.title)).setText(titlePlay);
        ((TextView) v.findViewById(R.id.title)).setTextColor(Color.parseColor(titleColor));

        String desc = description.trim();
        if (desc.length() > 47) desc = desc.substring(0, 47) + "...";
        final TextView tvDesc = (TextView) v.findViewById(R.id.description);
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.BLACK);

        v.findViewById(R.id.stripe).setBackgroundColor(Color.parseColor(color));
        ((NetworkImageView) v.findViewById(R.id.appIcon)).setImageUrl(
                String.format("https://api.nameless-rom.org/app/%s/icon", mAppId),
                ImageCacheManager.getInstance().getImageLoader()
        );

        if (isClickable) {
            v.findViewById(R.id.cardContainer)
                    .setBackgroundResource(R.drawable.selectable_background_cardbank);
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
