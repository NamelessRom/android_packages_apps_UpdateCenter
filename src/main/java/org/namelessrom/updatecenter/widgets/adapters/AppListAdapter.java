package org.namelessrom.updatecenter.widgets.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.AppListEvent;
import org.namelessrom.updatecenter.events.SubFragmentEvent;
import org.namelessrom.updatecenter.fragments.apps.AppDetailsFragment;
import org.namelessrom.updatecenter.items.AppData;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.cards.AppCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.updatecenter.Application.logDebug;

public class AppListAdapter extends BaseAdapter implements Constants {

    private final Context       mContext;
    private final List<AppData> mData;
    private int mCount   = 0;
    private int mCounter = 0;

    private boolean isLoading;
    private boolean isFirstTime = true;

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
        private TextView  mDev;
        private ImageView mIcon;

        public ViewHolder(final View v) {
            mTitle = findById(v, R.id.title);
            mDev = findById(v, R.id.developer);
            mIcon = findById(v, R.id.appIcon);
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (shouldLoadMoreData(mData, position)) {
            loadMoreData();
        }

        final AppData appData = mData.get(position);
        if (convertView == null) {
            convertView = new AppCard(mContext);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTitle.setText(appData.getTitle());
        viewHolder.mTitle.setTextColor(Color.parseColor("#669900"));

        viewHolder.mDev.setText(appData.getDeveloper());
        viewHolder.mDev.setTextColor(Color.BLACK);

        Ion.with(viewHolder.mIcon)
                .error(R.drawable.ic_warning)
                .load(String.format(APP_IMAGE_URL, appData.getAppId()));

        ((AppCard) convertView).setOnCardClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Fragment f = new AppDetailsFragment();
                final Bundle bundle = new Bundle(1);
                bundle.putSerializable(AppDetailsFragment.ARG_APP_DATA, appData);
                f.setArguments(bundle);
                BusProvider.getBus().post(new SubFragmentEvent(ID_APP_DETAILS, f));
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(final int i) { return 0; }

    @Override
    public int getCount() { return mData.size(); }

    @Override
    public Object getItem(final int i) { return mData.get(i); }

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
        Ion.with(mContext).load(url).as(AppData[].class).setCallback(
                new FutureCallback<AppData[]>() {
                    @Override
                    public void onCompleted(Exception e, AppData[] result) {
                        if (result != null) {
                            mData.addAll(Arrays.asList(result));

                            moreDataToLoad = result.length > 0 && mData.size() < mCount;

                            notifyDataSetChanged();

                            if (isFirstTime) {
                                isFirstTime = false;
                                BusProvider.getBus().post(new AppListEvent());
                            }
                        }
                        isLoading = false;
                    }
                }
        );
    }
}
