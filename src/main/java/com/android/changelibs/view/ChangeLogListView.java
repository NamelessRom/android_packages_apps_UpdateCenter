/*******************************************************************************
 * Copyright (c) 2013 Gabriele Mariotti.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.android.changelibs.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.changelibs.Constants;
import com.android.changelibs.Util;
import com.android.changelibs.internal.ChangeLog;
import com.android.changelibs.internal.ChangeLogAdapter;
import com.android.changelibs.internal.ChangeLogRow;
import com.android.changelibs.parser.XmlParser;

import org.namelessrom.updatecenter.R;

/**
 * ListView for ChangeLog
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class ChangeLogListView extends ListView implements AdapterView.OnItemClickListener {

    //--------------------------------------------------------------------------
    // Custom Attrs
    //--------------------------------------------------------------------------
    protected int    mRowLayoutId              = Constants.mRowLayoutId;
    protected int    mRowHeaderLayoutId        = Constants.mRowHeaderLayoutId;
    protected String mChangeLogFileResourceUrl = null;

    //--------------------------------------------------------------------------
    protected static String TAG = "ChangeLogListView";
    // Adapter
    protected ChangeLogAdapter mAdapter;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public ChangeLogListView(Context context) {
        super(context);
        init(null, 0);
    }

    public ChangeLogListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ChangeLogListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    //--------------------------------------------------------------------------
    // Init
    //--------------------------------------------------------------------------

    /**
     * Initialize
     *
     * @param attrs
     * @param defStyle
     */
    protected void init(AttributeSet attrs, int defStyle) {
        //Init attrs
        initAttrs(attrs, defStyle);
        //Init adapter
        initAdapter();

        //Set divider to 0dp
        setDividerHeight(0);
    }

    /**
     * Init custom attrs.
     *
     * @param attrs
     * @param defStyle
     */
    protected void initAttrs(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChangeLogListView, defStyle, defStyle);

        try {
            //Layout for rows and header
            mRowLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowLayoutId, mRowLayoutId);
            mRowHeaderLayoutId = a.getResourceId(R.styleable.ChangeLogListView_rowHeaderLayoutId,
                    mRowHeaderLayoutId);

            mChangeLogFileResourceUrl =
                    a.getString(R.styleable.ChangeLogListView_changeLogFileResourceUrl);
            //String which is used in header row for Version
            //mStringVersionHeader= a.getResourceId(R.styleable
            // .ChangeLogListView_StringVersionHeader,mStringVersionHeader);

        } finally {
            if (a != null) a.recycle();
        }
    }

    /**
     * Init adapter
     */
    protected void initAdapter() {

        try {
            //Read and parse changelog.xml
            XmlParser parse;
            if (mChangeLogFileResourceUrl != null) {
                parse = new XmlParser(getContext(), mChangeLogFileResourceUrl);
            } else {
                parse = new XmlParser(getContext());
            }
            //ChangeLog chg=parse.readChangeLogFile();
            final ChangeLog chg = new ChangeLog();

            //Create adapter and set custom attrs
            mAdapter = new ChangeLogAdapter(getContext(), chg.getRows());
            mAdapter.setmRowLayoutId(mRowLayoutId);
            mAdapter.setmRowHeaderLayoutId(mRowHeaderLayoutId);

            //Parse in a separate Thread to avoid UI block with large files
            if (mChangeLogFileResourceUrl == null || (mChangeLogFileResourceUrl != null && Util
                    .isConnected(getContext()))) {
                new ParseAsyncTask(mAdapter, parse).execute();
            }
            setAdapter(mAdapter);
        } catch (Exception ignored) { }

    }

    public void loadFromUrl(final String url) {
        try {
            final Context context = getContext();
            final XmlParser parser = new XmlParser(context, url);

            ChangeLog chg = new ChangeLog();

            //Create adapter and set custom attrs
            mAdapter = new ChangeLogAdapter(context, chg.getRows());
            mAdapter.setmRowLayoutId(mRowLayoutId);
            mAdapter.setmRowHeaderLayoutId(mRowHeaderLayoutId);

            //Parse in a separate Thread to avoid UI block with large files
            if (Util.isConnected(context)) {
                new ParseAsyncTask(mAdapter, parser).execute();
            }
            setAdapter(mAdapter);
        } catch (Exception ignored) { }
    }

    /**
     * Async Task to parse xml file in a separate thread
     */
    protected class ParseAsyncTask extends AsyncTask<Void, Void, ChangeLog> {

        private ChangeLogAdapter mAdapter;
        private XmlParser        mParse;

        public ParseAsyncTask(ChangeLogAdapter adapter, XmlParser parse) {
            mAdapter = adapter;
            mParse = parse;
        }

        @Override
        protected ChangeLog doInBackground(Void... params) {

            try {
                if (mParse != null) {
                    return mParse.readChangeLogFile();
                }
            } catch (Exception ignored) { }
            return null;
        }

        protected void onPostExecute(ChangeLog chg) {

            //Notify data changed
            if (chg != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mAdapter.addAll(chg.getRows());
                } else {
                    if (chg.getRows() != null) {
                        for (ChangeLogRow row : chg.getRows()) {
                            mAdapter.add(row);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * Sets the list's adapter, enforces the use of only a ChangeLogAdapter
     */
    public void setAdapter(ChangeLogAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO
    }


}
