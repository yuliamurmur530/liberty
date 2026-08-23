package net.crinoidea.liberty.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.VpnService;
import android.os.Build;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AppCatalog {
    private AppCatalog() {
    }

    public static List<AppEntry> loadLaunchableApps(Context context, boolean includeSystemApps) {
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            resolved = packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL)
            );
        } else {
            resolved = queryLegacy(packageManager, launcherIntent);
        }

        Set<String> seenPackages = new HashSet<>();
        Set<String> vpnPackages = loadVpnPackages(packageManager);
        List<AppEntry> apps = new ArrayList<>();
        String ownPackage = context.getPackageName();

        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                continue;
            }

            String packageName = info.activityInfo.packageName;
            if (packageName == null
                    || packageName.equals(ownPackage)
                    || vpnPackages.contains(packageName)
                    || !seenPackages.add(packageName)) {
                continue;
            }

            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            boolean systemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean appStore = "com.android.vending".equals(packageName);
            if (!includeSystemApps && systemApp && !appStore) {
                continue;
            }

            CharSequence rawLabel = info.loadLabel(packageManager);
            String label = rawLabel == null ? packageName : rawLabel.toString().trim();
            apps.add(new AppEntry(label, packageName, info));
        }

        Collator collator = Collator.getInstance(new Locale("ru"));
        collator.setStrength(Collator.PRIMARY);
        apps.sort((left, right) -> collator.compare(left.label(), right.label()));
        return apps;
    }

    @SuppressWarnings("deprecation")
    private static List<ResolveInfo> queryLegacy(PackageManager manager, Intent intent) {
        return manager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
    }

    private static Set<String> loadVpnPackages(PackageManager packageManager) {
        Intent vpnIntent = new Intent(VpnService.SERVICE_INTERFACE);
        List<ResolveInfo> services;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            services = packageManager.queryIntentServices(
                    vpnIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL)
            );
        } else {
            services = queryVpnServicesLegacy(packageManager, vpnIntent);
        }

        Set<String> packages = new HashSet<>();
        for (ResolveInfo service : services) {
            if (service.serviceInfo != null && service.serviceInfo.packageName != null) {
                packages.add(service.serviceInfo.packageName);
            }
        }
        return packages;
    }

    @SuppressWarnings("deprecation")
    private static List<ResolveInfo> queryVpnServicesLegacy(
            PackageManager manager,
            Intent intent
    ) {
        return manager.queryIntentServices(intent, PackageManager.MATCH_ALL);
    }
}
