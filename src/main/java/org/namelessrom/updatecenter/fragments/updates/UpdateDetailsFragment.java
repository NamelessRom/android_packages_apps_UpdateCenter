package org.namelessrom.updatecenter.fragments.updates;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.changelibs.view.ChangeLogListView;
import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.UpdateHelper;
import org.namelessrom.updatecenter.widgets.AttachFragment;

/**
 * Created by alex on 28.04.14.
 */
public class UpdateDetailsFragment extends AttachFragment implements Constants {

    public static final String ARG_UPDATE_INFO = "arg_update_info";

    private UpdateInfo mUpdateInfo;

    private TextView mChannel;
    private TextView mTitle;
    private TextView mInfo;
    private Button   mAction;

    private ProgressBar mProgressBar;

    private ChangeLogListView mChangelog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID_UPDATE_DETAILS);
        activity.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        BusProvider.getBus().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.unregisterReceiver(receiver);
            }
        } catch (Exception ignored) { /* Not registered, nothing to do */ }

        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final Bundle arguments = getArguments();
        mUpdateInfo = (UpdateInfo) arguments.getSerializable(ARG_UPDATE_INFO);

        final View v = inflater.inflate(R.layout.fragment_update_details, container, false);

        mChannel = (TextView) v.findViewById(R.id.updateChannel);
        mTitle = (TextView) v.findViewById(R.id.updateTitle);
        mInfo = (TextView) v.findViewById(R.id.updateInfo);
        mAction = (Button) v.findViewById(R.id.updateAction);

        mProgressBar = (ProgressBar) v.findViewById(R.id.appProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mChangelog = (ChangeLogListView) v.findViewById(R.id.updateChangelog);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChannel.setText(mUpdateInfo.getUpdateChannelShort());
        mTitle.setText(mUpdateInfo.getUpdateTimeStamp());
        mInfo.setText(mUpdateInfo.getUpdateName());

        updateButton();

        mChangelog.loadFromUrl(mUpdateInfo.getUpdateUrl()
                .replace("/download", ".changelog/download"));
    }

    private void updateButton() {
        final int textId;
        final int state;
        DownloadItem tmpItem = null;

        if (Helper.isUpdateDownloaded(mUpdateInfo.getUpdateName())) {
            textId = R.string.not_action_install_update;
            state = Constants.UPDATE_DOWNLOADED;
        } else {
            if (Application.mDownloadItems != null) {
                for (final DownloadItem item : Application.mDownloadItems) {
                    if (item.getMd5().equals(mUpdateInfo.getUpdateMd5())) {
                        if (item.getCompleted().equals("0")) {
                            tmpItem = item;
                            break;
                        }
                    }
                }
            }

            if (tmpItem != null) {
                textId = R.string.cancel_download;
                state = Constants.UPDATE_DOWNLOADING;
            } else {
                textId = R.string.download;
                state = 0;
            }
        }

        final DownloadItem downloadItem = tmpItem;
        mAction.setText(textId);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateHelper.getDialog(getActivity(), state, mUpdateInfo,
                        downloadItem).show();
            }
        });
    }

    @Subscribe
    public void onRefreshEvent(final RefreshEvent event) {
        if (event == null) {
            return;
        }

        Application.sHandler.removeCallbacks(mRefreshRunnable);
        Application.sHandler.postDelayed(mRefreshRunnable, 250);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onRefreshEvent(new RefreshEvent());
        }
    };

    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            updateButton();
        }
    };

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            default:
                break;
        }

        return false;
    }
}
