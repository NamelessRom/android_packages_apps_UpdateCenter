package org.namelessrom.updatecenter.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;

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

}
