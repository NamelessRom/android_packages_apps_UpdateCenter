package org.namelessrom.updatecenter.utils;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.namelessrom.updatecenter.Application;

import java.util.List;

/**
 * Created by alex on 28.04.14.
 */
public class AppHelper {

    public static ApplicationInfo getApplicationInfo(final PackageManager pm,
            final String targetPackage) {
        final List<ApplicationInfo> packages;
        if (pm != null) {
            packages = pm.getInstalledApplications(0);
            for (final ApplicationInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(targetPackage)) return packageInfo;
            }
        }
        return null;
    }

    public static boolean isPackageInstalled(final PackageManager pm, final String targetPackage) {
        return getApplicationInfo(pm, targetPackage) != null;
    }

    public static void installPackage(final PackageManager pm, final Uri uri, final int flags) {
        installPackage(pm, null, uri, flags);
    }

    public static void installPackage(final PackageManager pm,
            final IPackageInstallObserver observer, final Uri uri, final int flags) {
        pm.installPackage(uri, observer, flags, "org.namelessrom.updatecenter");
    }

    public static void disableComponent(final String packageName, final String componentName) {
        toggleComponent(packageName, componentName, true);
    }

    public static void enableComponent(final String packageName, final String componentName) {
        toggleComponent(packageName, componentName, false);
    }

    private static void toggleComponent(final String packageName, final String componentName,
            boolean disable) {
        final ComponentName component = new ComponentName(packageName,
                packageName + componentName);
        final PackageManager pm = Application.packageManager;
        if (pm != null) {
            pm.setComponentEnabledSetting(component,
                    (disable
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
                    PackageManager.DONT_KILL_APP
            );
        }
    }

}
