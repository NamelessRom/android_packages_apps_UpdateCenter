package org.namelessrom.updatecenter.fragments.apps;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.updatecenter.Application;
import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.events.SectionAttachedEvent;
import org.namelessrom.updatecenter.items.AppData;
import org.namelessrom.updatecenter.utils.AppHelper;
import org.namelessrom.updatecenter.utils.BusProvider;
import org.namelessrom.updatecenter.utils.Constants;
import org.namelessrom.updatecenter.widgets.AttachFragment;

import java.io.File;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.updatecenter.Application.logDebug;

/**
 * Created by alex on 28.04.14.
 */
public class AppDetailsFragment extends AttachFragment implements Constants {

    public static final String ARG_APP_DATA = "arg_app_data";

    private static final Handler mHandler = new Handler();

    private AppData mAppData;

    private ImageView mAppIcon;
    private TextView  mAppName;
    private TextView  mAppDeveloper;
    private Button    mActionInstall;
    private Button    mActionInstalled;

    private ProgressBar mProgressBar;

    private TextView mAppDescription;

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

        mAppIcon = findById(v, R.id.appIcon);
        mAppName = findById(v, R.id.appName);
        mAppDeveloper = findById(v, R.id.appDeveloper);
        mActionInstall = findById(v, R.id.action_install);
        mActionInstalled = findById(v, R.id.action_installed);

        mProgressBar = findById(v, R.id.appProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mAppDescription = findById(v, R.id.appDescription);

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

                        mHandler.post(mUpdateViews);
                    }
                });
    }

    private final Runnable mUpdateViews = new Runnable() {
        @Override
        public void run() {
            updateViews();
        }
    };

    private void updateViews() {
        final String pkgName = mAppData.getAppId();
        final ApplicationInfo applicationInfo =
                AppHelper.getApplicationInfo(Application.packageManager, pkgName);
        if (applicationInfo != null) {
            mActionInstalled.setVisibility(View.VISIBLE);
            if (hasUpdate(pkgName)) {
                mActionInstalled.setText("Update");
                mActionInstalled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchAppInstall(pkgName);
                    }
                });
            } else {
                mActionInstalled.setText("Open");
                mActionInstalled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(
                                Application.packageManager.getLaunchIntentForPackage(pkgName));
                    }
                });
            }

            mActionInstall.setText("Uninstall");
            mActionInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Application.packageManager.deletePackage(
                            pkgName, new PackageDeleteObserver(), 0);
                }
            });
        } else {
            mActionInstalled.setVisibility(View.INVISIBLE);
            mActionInstall.setText("Install");
            mActionInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchAppInstall(pkgName);
                }
            });
        }
    }

    private boolean hasUpdate(final String pkgName) {
        boolean showUpdate = false;
        try {
            final PackageInfo packageInfo =
                    Application.packageManager.getPackageInfo(pkgName, 0);
            if (packageInfo != null) {
                showUpdate = mAppData.getVersionCode() > packageInfo.versionCode;
            }
        } catch (Exception ignored) { }
        return showUpdate;
    }

    private void launchAppInstall(final String pkgName) {
        final File f = new File(Application.applicationContext.getCacheDir(),
                pkgName + ".apk");
        if (f.exists()) {
            logDebug("Deleted " + f.getName() + ": " + String.valueOf(f.delete()));
        }

        mProgressBar.setVisibility(View.VISIBLE);
        Ion.with(AppDetailsFragment.this).load(String.format(APP_PACKAGE_URL, pkgName))
                .progressBar(mProgressBar)
                .noCache()
                .write(f).setCallback(new FutureCallback<File>() {
            @Override
            public void onCompleted(Exception e, File result) {
                mProgressBar.setVisibility(View.INVISIBLE);
                result.setReadable(true, false);
                AppHelper.installPackage(Application.packageManager,
                        new PackageInstallObserver(), Uri.fromFile(result),
                        PackageManager.INSTALL_REPLACE_EXISTING);
            }
        });
    }

    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Log.e("PM", "packageDeleted");
            logDebug("Removed package: " + packageName + " | " + String.valueOf(returnCode));
            mHandler.post(mUpdateViews);
        }
    }

    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            Log.e("PM", "packageDeleted");
            logDebug("Removed package: " + packageName + " | " + String.valueOf(returnCode));
            mHandler.post(mUpdateViews);
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
