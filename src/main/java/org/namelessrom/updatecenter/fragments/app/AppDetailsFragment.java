package org.namelessrom.updatecenter.fragments.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.items.AppData;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachFragment;

/**
 * Created by alex on 28.04.14.
 */
public class AppDetailsFragment extends AttachFragment implements Constants {

    public static final String ARG_APP_DATA = "arg_app_data";

    private AppData mAppData;

    private ImageView mAppIcon;
    private TextView  mAppName;
    private TextView  mAppDeveloper;
    private Button    mActionInstall;
    private Button    mActionInstalled;

    private TextView mAppDescription;

    // TODO: real check
    private boolean mIsInstalled = false;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_APP_DETAILS); }

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
        mAppData = (AppData) arguments.getSerializable(ARG_APP_DATA);

        final View v = inflater.inflate(R.layout.fragment_app_details, container, false);

        mAppIcon = (ImageView) v.findViewById(R.id.appIcon);
        mAppName = (TextView) v.findViewById(R.id.appName);
        mAppDeveloper = (TextView) v.findViewById(R.id.appDeveloper);
        mActionInstall = (Button) v.findViewById(R.id.action_install);
        mActionInstalled = (Button) v.findViewById(R.id.action_installed);

        mAppDescription = (TextView) v.findViewById(R.id.appDescription);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Ion.with(mAppIcon)
                .error(R.drawable.ic_warning)
                .load(String.format(APP_IMAGE_URL, mAppData.getAppId()));

        Ion.with(this).load(String.format(APP_DETAILS_URL, mAppData.getAppId()))
                .as(AppData[].class)
                .setCallback(new FutureCallback<AppData[]>() {
                    @Override
                    public void onCompleted(Exception e, AppData[] result) {
                        // TODO: show error
                        if (e != null) Log.e("UpdateCenter", "Error: " + e.getMessage());

                        mAppData = result[0];
                        mAppName.setText(mAppData.getTitle());
                        mAppDeveloper.setText(mAppData.getDeveloper());
                        mAppDescription.setText(mAppData.getDescription());

                        // TODO: real check
                        mActionInstall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mIsInstalled = !mIsInstalled;
                                updateViews();
                            }
                        });

                        updateViews();
                    }
                });
    }

    private void updateViews() {
        if (mIsInstalled) {
            mActionInstalled.setVisibility(View.VISIBLE);
            mActionInstalled.setText("Open");
            mActionInstall.setText("Uninstall");
        } else {
            mActionInstalled.setVisibility(View.INVISIBLE);
            mActionInstall.setText("Install");
        }
    }

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
