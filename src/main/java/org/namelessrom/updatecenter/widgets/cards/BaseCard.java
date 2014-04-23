package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.namelessrom.updatecenter.R;

public abstract class BaseCard extends LinearLayout {

    protected boolean mHasOverflow;
    protected View    mContainer;

    protected View mOverflow = null;

    public BaseCard(final Context context) { this(context, false); }

    public BaseCard(final Context context, final boolean hasOverflow) {
        super(context);

        mHasOverflow = hasOverflow;
        setupView(context);
    }

    public View setupView(Context context) {
        final View view = inflate(context, R.layout.card_item_card, this);

        assert view != null;

        ((FrameLayout) view.findViewById(R.id.cardContent)).addView(getCardContent(context));

        view.requestLayout();
        return view;
    }

    public abstract View getCardContent(final Context context);

    public void setOnCardClickListener(final View.OnClickListener listener) {
        if (mContainer != null) {
            mContainer.setOnClickListener(listener);
        }
    }

    public void setOnCardLongClickListener(final View.OnLongClickListener onLongListener) {
        if (mContainer != null) {
            mContainer.setOnLongClickListener(onLongListener);
        }
    }

    public View getOverflow() {
        return mOverflow;
    }

    public void setOverflow(final int type) {
        if (mOverflow != null) {
            mOverflow.setVisibility(type);
        }
    }

}
