package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.namelessrom.updatecenter.R;

public abstract class BaseCard extends LinearLayout {

    protected OnClickListener     mListener;
    protected OnLongClickListener onLongListener;

    public BaseCard(final Context context) {
        super(context);

        setupView(context);
    }

    public View setupView(Context context) {
        final View view = inflate(context, R.layout.card_item_card, this);

        assert view != null;

        ((FrameLayout) view.findViewById(R.id.cardContent)).addView(getCardContent(context));

        view.requestLayout();
        return view;
    }

    public abstract View getCardContent(Context context);

    public OnClickListener getOnClickListener() {
        return mListener;
    }

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public OnLongClickListener getOnLongClickListener() {
        return onLongListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongListener) {
        this.onLongListener = onLongListener;
    }

}
