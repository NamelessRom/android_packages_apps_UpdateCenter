package org.namelessrom.updatecenter.widgets.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.IdEvent;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.cards.AppCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.namelessrom.updatecenter.Application.logDebug;

public class AppListAdapter extends BaseAdapter implements Constants {

    private final List<AppData> mData;
    private int mCount   = 0;
    private int mCounter = 0;

    private Context mContext;
    private boolean isLoading;

    private boolean moreDataToLoad;

    public AppListAdapter(final Context context) {
        super();
        mContext = context;
        mData = new ArrayList<AppData>();

        Ion.with(mContext).load(APP_COUNT_URL).asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                try {
                    mCount = new JSONObject(result).getInt("count");
                } catch (Exception exc) { mCount = 0; }

                moreDataToLoad = true;
                loadMoreData();
            }
        });
    }

    private static class ViewHolder {
        private TextView  mTitle;
        private TextView  mDesc;
        private ImageView mIcon;
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
            viewHolder.mIcon = (ImageView) v.findViewById(R.id.appIcon);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        viewHolder.mTitle.setText(appData.getTitle());
        viewHolder.mTitle.setTextColor(Color.parseColor("#669900"));

        String desc = appData.getDescription().trim();
        logDebug("desc full: " + desc);
        if (desc.length() > 47) desc = desc.substring(0, 47) + "...";
        logDebug("desc substring: " + desc);

        viewHolder.mDesc.setText(desc);
        viewHolder.mDesc.setTextColor(Color.BLACK);

        Ion.with(viewHolder.mIcon)
                .error(R.drawable.ic_warning)
                .load(String.format(APP_IMAGE_URL, appData.getAppId()));

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
        final String url = String.format(APP_URL + APP_URL_QUERY,
                String.valueOf(mCounter * 10)); //-------------0, 10, 20, 30, ...
        logDebug("loadMoreData: " + url);
        mCounter++;
        Ion.with(mContext).load(url).as(AppData[].class)
                .setCallback(new FutureCallback<AppData[]>() {
                    @Override
                    public void onCompleted(Exception e, AppData[] result) {
                        if (result != null) {
                            mData.addAll(Arrays.asList(result));

                            moreDataToLoad = result.length > 0 && mData.size() < mCount;

                            notifyDataSetChanged();
                        }
                        isLoading = false;
                        BusProvider.getBus().post(new IdEvent(IdEvent.ID_APP_LIST_LOADED));
                    }
                });
    }
}
