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
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.otto.Subscribe;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.database.DatabaseHandler;
import org.namelessrom.updatecenter.database.DownloadItem;
import org.namelessrom.updatecenter.events.RefreshEvent;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.items.UpdateInfo;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.utils.FileUtils;
import org.namelessrom.updatecenter.utils.Helper;
import org.namelessrom.updatecenter.utils.UpdateHelper;
import org.namelessrom.updatecenter.widgets.AttachFragment;

import java.io.File;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.updatecenter.Application.logDebug;

/**
 * Created by alex on 28.04.14.
 */
public class UpdateDetailsFragment extends AttachFragment implements Constants {

    public static final String ARG_UPDATE_INFO = "arg_update_info";

    private UpdateInfo mUpdateInfo;

    private TextView    mChannel;
    private TextView    mTitle;
    private TextView    mInfo;
    private ImageButton mAction;
    private ImageButton mExtra;

    private ProgressBar mChangelogLoading;
    private WebView     mChangelog;

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
        assert (arguments != null);
        mUpdateInfo = (UpdateInfo) arguments.getSerializable(ARG_UPDATE_INFO);

        final View v = inflater.inflate(R.layout.fragment_update_details, container, false);

        mChannel = findById(v, R.id.updateChannel);
        mTitle = findById(v, R.id.updateTitle);
        mInfo = findById(v, R.id.updateInfo);
        mAction = findById(v, R.id.updateAction);
        mExtra = findById(v, R.id.updateExtra);

        mChangelogLoading = findById(v, R.id.updateChangelogLoading);
        mChangelog = findById(v, R.id.updateChangelog);
        mChangelog.getSettings().setJavaScriptEnabled(true);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String tmp = mUpdateInfo.getTimestamp();
        mChannel.setText(mUpdateInfo.getChannelShort());
        mTitle.setText(!tmp.equals("-") ? tmp : "");
        mInfo.setText(mUpdateInfo.getName());

        updateButton();

        final File changelog = new File(Application.getFilesDirectory() + "/changelogs"
                + '/' + mUpdateInfo.getName() + ".changelog");
        boolean success = false;
        if (changelog.exists()) {
            try {
                loadData(FileUtils.readFromFile(changelog));
                success = true;
            } catch (Exception exc) {
                logDebug(exc.getMessage());
                success = false;
            }
        }
        if (!success) {
            Ion.with(this)
                    .load(mUpdateInfo.getUrl().replace("/download", ".changelog/download"))
                    .asString().setCallback(new FutureCallback<String>() {
                @Override public void onCompleted(final Exception e, String result) {
                    if (e != null) {
                        loadData(e.getLocalizedMessage());
                        return;
                    }
                    result = result.replace("/css/bootstrap.min.css",
                            "file:///android_asset/css/bootstrap.min.css")
                            .replace("/js/jquery.min.js", "file:///android_asset/js/jquery.min.js")
                            .replace("/js/main.js", "file:///android_asset/js/main.js");
                    try {
                        FileUtils.writeToFile(changelog, result);
                    } catch (Exception exc) { logDebug(exc.getMessage()); }
                    loadData(result);
                }
            });
        }
    }

    private void loadData(final String data) {
        if (mChangelog != null && data != null) {
            mChangelog
                    .loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", "");
            mChangelogLoading.setVisibility(View.INVISIBLE);
        }
    }

    private void updateButton() {
        final boolean isDownloading;
        final int imageId;
        final int state;
        boolean downloaded = false;
        DownloadItem tmpItem = null;

        if (Application.mDownloadItems != null) {
            for (final DownloadItem item : Application.mDownloadItems) {
                if (item.getMd5().equals(mUpdateInfo.getMd5())) {
                    if (item.getCompleted().equals("0")) {
                        tmpItem = item;
                        downloaded = false;
                        break;
                    } else if (item.getCompleted().equals("1")) {
                        tmpItem = item;
                        downloaded = true;
                        break;
                    }
                }
            }
        }

        if (tmpItem != null) {
            if (downloaded && Helper.isUpdateDownloaded(mUpdateInfo.getName())) {
                isDownloading = false;
                imageId = R.drawable.ic_action_install;
                state = Constants.UPDATE_DOWNLOADED;
            } else {
                isDownloading = true;
                imageId = R.drawable.ic_action_cancel;
                state = Constants.UPDATE_DOWNLOADING;
            }
        } else {
            isDownloading = false;
            imageId = R.drawable.ic_action_download;
            state = 0;
        }

        final DownloadItem downloadItem = tmpItem;
        mAction.setImageResource(imageId);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateHelper.getDialog(getActivity(), state, mUpdateInfo, downloadItem).show();
            }
        });

        if (false /* TODO rewrite to add pause/resume support */ && isDownloading) {
            mExtra.setImageResource(downloadItem.isPaused()
                    ? R.drawable.ic_action_resume : R.drawable.ic_action_pause);
            mExtra.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    final long downloadId = Long.parseLong(downloadItem.getDownloadId());
                    logDebug("downloadItem.getDownloadId(): " + String.valueOf(downloadId));
                    if (downloadItem.isPaused()) {
                        // TODO
                    } else {
                        // TODO
                    }
                    downloadItem.setPaused(!downloadItem.isPaused());

                    Application.getDb().updateItem(downloadItem, DatabaseHandler.TABLE_DOWNLOADS);
                    Application.mDownloadItems = Application.getDb()
                            .getAllItems(DatabaseHandler.TABLE_DOWNLOADS);
                    BusProvider.getBus().post(new RefreshEvent());
                }
            });
            mExtra.setVisibility(View.VISIBLE);
        } else {
            mExtra.setVisibility(View.INVISIBLE);
        }
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
