package org.namelessrom.updatecenter.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;

public class MenuListArrayAdapter extends BaseAdapter {

    private final Context  mContext;
    private final int      mLayoutResourceId;
    private final String[] mTitles;
    private final int[]    mIcons;

    private Drawable userAvatar;

    public MenuListArrayAdapter(final Context context, final int layoutResourceId,
            final String[] titles, final int[] icons) {

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mTitles = titles;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2; //return 2, you have two types that the getView() method will return,
        // normal(0) and for the last row(1)
    }

    @Override
    public int getItemViewType(int position) {
        return (mIcons[position] == -1) ? 1 : 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 1;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        final int type = getItemViewType(position);
        if (v == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (type == 0) {
                v = inflater.inflate(mLayoutResourceId, parent, false);
            } else if (type == 1) {
                v = inflater.inflate(R.layout.menu_header, parent, false);
            }
        }

        final int defaultColor = mContext.getResources().getColor(android.R.color.black);
        if (type == 0) {
            final TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            text1.setText(mTitles[position]);
            text1.setTextColor(Color.BLACK);

            final ImageView image = (ImageView) v.findViewById(R.id.image);
            final int imageRes = mIcons[position];
            if (imageRes == 0) {
                image.setVisibility(View.GONE);
            } else {
                image.setImageResource(mIcons[position]);
                image.setColorFilter(Color.parseColor("#000000"));
                image.setColorFilter(defaultColor);
            }
        } else if (type == 1) {
            final TextView header = (TextView) v.findViewById(R.id.menu_header);
            header.setText(mTitles[position]);
            header.setClickable(false);
            header.setTextColor(defaultColor);
        }

        return v;
    }

}

