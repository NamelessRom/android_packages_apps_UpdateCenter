package org.namelessrom.updatecenter.widgets.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.net.images.ImageCacheManager;
import org.namelessrom.updatecenter.net.requests.GsonRequest;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.cards.AppCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppListAdapter extends BaseAdapter
        implements Listener<AppData[]>, ErrorListener {

    private final List<AppData> mData;

    private Context mContext;
    private boolean isLoading;

    private boolean moreDataToLoad;

    public AppListAdapter(final Context context) {
        super();
        mData = new ArrayList<AppData>();
        mContext = context;

        moreDataToLoad = true;

        loadMoreData();
    }

    private static class ViewHolder {
        private TextView         mTitle;
        private TextView         mDesc;
        private NetworkImageView mIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder viewHolder;

        if (shouldLoadMoreData(mData, position)) {
            loadMoreData();
        }

        final AppData appData = mData.get(position);
        if (v == null) {
            v = new AppCard(mContext);

            viewHolder = new ViewHolder();
            viewHolder.mTitle = (TextView) v.findViewById(R.id.title);
            viewHolder.mDesc = (TextView) v.findViewById(R.id.description);
            viewHolder.mIcon = (NetworkImageView) v.findViewById(R.id.appIcon);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        viewHolder.mTitle.setText(appData.getTitle());
        viewHolder.mTitle.setTextColor(Color.parseColor("#669900"));

        String desc = appData.getDescription().trim();
        if (desc.length() > 47) desc = desc.substring(0, 47) + "...";

        viewHolder.mDesc.setText(desc);
        viewHolder.mDesc.setTextColor(Color.BLACK);

        viewHolder.mIcon.setImageUrl(
                String.format("https://api.nameless-rom.org/app/%s/icon", appData.getAppId()),
                ImageCacheManager.getInstance().getImageLoader()
        );

        return v;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    public void add(final List<AppData> newData) {
        isLoading = false;
        if (!newData.isEmpty()) {
            mData.addAll(newData);
            notifyDataSetChanged();
        }
    }

    private boolean shouldLoadMoreData(List<AppData> data, int position) {
        boolean scrollRangeReached = (position > (data.size() - 4));
        return (scrollRangeReached && !isLoading && moreDataToLoad);
    }

    private void loadMoreData() {
        isLoading = true;
        Application.addToRequestQueue(new GsonRequest(Request.Method.GET,
                "https://api.nameless-rom.org/app", AppData[].class, this, this));
    }

    @Override
    public void onResponse(final AppData[] response) {
        if (response != null) {
            mData.addAll(Arrays.asList(response));

            moreDataToLoad = response.length > 0 && mData.size() < 100; // just a test

            notifyDataSetChanged();
        }

        isLoading = false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        isLoading = false;
    }

}
