package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import org.namelessrom.updatecenter.R;

public class AppCard extends BaseCard {

    private boolean mHasOverflow;

    public AppCard(final Context context) {
        this(context, false);
    }

    public AppCard(final Context context, final boolean hasOverflow) {
        super(context);
        mHasOverflow = hasOverflow;
    }

    @Override
    public View getCardContent(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.card_app, null, false);

        assert v != null;

        final View mStripe = v.findViewById(R.id.stripe);
        mStripe.setBackgroundColor(Color.parseColor("#4ac925"));

        final View mContainer = v.findViewById(R.id.cardContainer);
        mContainer.setBackgroundResource(R.drawable.selectable_background_cardbank);

        if (mHasOverflow) {
            v.findViewById(R.id.overflow).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.overflow).setVisibility(View.GONE);
        }

        return v;
    }

}
