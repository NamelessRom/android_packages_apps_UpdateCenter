package org.namelessrom.updatecenter.widgets.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.fima.cardsui.StackAdapter;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.net.requests.GsonRequest;
import org.namelessrom.updatecenter.utils.items.AppData;
import org.namelessrom.updatecenter.widgets.cards.AppCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Trey Robinson
 */
public class AppListAdapter extends ArrayAdapter<String>
        implements Listener<AppData[]>, ErrorListener {


    private final String TAG = getClass().getSimpleName();

    /**
     * The data that drives the adapter
     */
    private final List<AppData> mData;

    /**
     * The last network response containing twitter metadata
     */
    private AppData[] mTweetData;

    private boolean isLoading;


    /**
     * Flag telling us our last network call returned 0 results and we do not need to execute any
     * new requests
     */
    private boolean moreDataToLoad;

    /**
     * @param context The context
     */
    public AppListAdapter(final Context context) {
        super(context, android.R.layout.simple_list_item_1);
        mData = new ArrayList<AppData>();
        mTweetData = mData.toArray(new AppData[mData.size()]);

        moreDataToLoad = true;

        loadMoreData();
    }

    private static class ViewHolder {
        final AppCard appCard;

        public ViewHolder(final AppCard card) {
            appCard = card;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder viewHolder;

        //check to see if we need to load more data
        if (shouldLoadMoreData(mData, position)) {
            loadMoreData();
        }

        final AppData appData = mData.get(position);
        if (v == null) {
            final AppCard card = new AppCard(appData.getAppId(), appData.getTitle(),
                    appData.getDescription());
            v = card.getCardContent(getContext());
        }

        return v;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    /**
     * Add the data to the current listview
     *
     * @param newData Data to be added to the listview
     */
    public void add(List<AppData> newData) {
        isLoading = false;
        if (!newData.isEmpty()) {
            mData.addAll(newData);
            notifyDataSetChanged();
        }
    }

    /**
     * a new request.
     *
     * @param data     Current list of data
     * @param position Current view position
     * @return
     */
    private boolean shouldLoadMoreData(List<AppData> data, int position) {
        // If showing the last set of data, request for the next set of data
        boolean scrollRangeReached = (position > (data.size() - 4));
        return (scrollRangeReached && !isLoading && moreDataToLoad);
    }

    private void loadMoreData() {
        isLoading = true;
        Log.v(getClass().toString(), "Load more tweets");
        Application.addToRequestQueue(new GsonRequest(Request.Method.GET,
                "https://api.nameless-rom.org/app", AppData[].class, this, this));
    }

    @Override
    public void onResponse(AppData[] response) {
        if (response != null) {
            mData.addAll(Arrays.asList(response));
            mTweetData = response;

            if (Arrays.asList(mTweetData).size() > 0 && Arrays.asList(mTweetData).size() < 10) {
                moreDataToLoad = true;
            } else {
                moreDataToLoad = false;
            }

            notifyDataSetChanged();
            Log.v(TAG, "New tweets retrieved");
        }

        isLoading = false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Error retrieving additional tweets: " + error.getMessage());
        isLoading = false;
    }

}
