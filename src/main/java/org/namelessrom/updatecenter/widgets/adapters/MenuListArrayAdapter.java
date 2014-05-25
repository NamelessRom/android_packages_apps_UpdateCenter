package org.namelessrom.updatecenter.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;

import static butterknife.ButterKnife.findById;

public class MenuListArrayAdapter extends BaseAdapter {

    private final Context  mContext;
    private final int      mLayoutResourceId;
    private final String[] mTitles;
    private final int[]    mIcons;

    public MenuListArrayAdapter(final Context context, final int layoutResourceId,
            final String[] titles, final int[] icons) {
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mTitles = titles;
        mIcons = icons;
    }

    @Override
    public int getCount() { return mTitles.length; }

    @Override
    public Object getItem(final int position) { return position; }

    @Override
    public long getItemId(final int arg0) { return 0; }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public int getItemViewType(final int position) { return (mIcons[position] == -1) ? 1 : 0; }

    @Override
    public boolean isEnabled(final int position) { return getItemViewType(position) != 1; }

    private static class ViewHolder {
        private final TextView  header;
        private final TextView  title;
        private final ImageView image;

        public ViewHolder(final View v, final int type) {
            if (type == 0) {
                header = null;
                title = findById(v, android.R.id.text1);
                image = findById(v, R.id.image);
            } else if (type == 1) {
                header = findById(v, R.id.menu_header);
                title = null;
                image = null;
            } else {
                header = null;
                title = null;
                image = null;
            }
        }
    }

    @Override
    public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        final int type = getItemViewType(position);
        if (v == null) {
            final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (type == 0) {
                v = inflater.inflate(mLayoutResourceId, parent, false);
            } else if (type == 1) {
                v = inflater.inflate(R.layout.menu_header, parent, false);
            }
            viewHolder = new ViewHolder(v, type);
            assert (v != null);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final int defaultColor = mContext.getResources().getColor(android.R.color.black);
        if (type == 0) {
            viewHolder.title.setText(mTitles[position]);
            viewHolder.title.setTextColor(Color.BLACK);

            final int imageRes = mIcons[position];
            if (imageRes == 0) {
                viewHolder.image.setVisibility(View.GONE);
            } else {
                viewHolder.image.setImageResource(mIcons[position]);
                viewHolder.image.setColorFilter(Color.parseColor("#000000"));
                viewHolder.image.setColorFilter(defaultColor);
            }
        } else if (type == 1) {
            viewHolder.header.setText(mTitles[position]);
            viewHolder.header.setClickable(false);
            viewHolder.header.setTextColor(defaultColor);
        }

        return v;
    }

}

