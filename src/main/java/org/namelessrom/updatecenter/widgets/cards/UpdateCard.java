package org.namelessrom.updatecenter.widgets.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import org.namelessrom.updatecenter.R;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 29.04.14.
 */
public class UpdateCard extends BaseCard {

    public UpdateCard(Context context) { super(context); }

    public UpdateCard(Context context, boolean hasOverflow) { super(context, hasOverflow); }

    @Override
    public View getCardContent(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.card_update, null, false);

        assert v != null;

        mContainer = findById(v, R.id.cardContainer);
        mOverflow = findById(v, R.id.overflow);

        setOverflow(mHasOverflow ? View.VISIBLE : View.GONE);

        return v;
    }

}
