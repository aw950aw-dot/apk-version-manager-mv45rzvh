package com.generated.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApkScanner {

    public interface ScanCallback {
        void onProgress(String currentPath, int count);
        void onComplete(List<ApkGroup> groups, List<ApkItem> allItems);
    }

    public static void scanDirectories(final Context context, final List<File> directories, final ScanCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = context.getPackageManager();
                List<ApkItem> rawItems = new ArrayList<>();

                for (File dir : directories) {
                    if (dir != null && dir.exists() && dir.isDirectory()) {
                        scanRecursive(pm, dir, rawItems, callback, handler);
                    }
                }

                Map<String, ApkGroup> groupMap = new HashMap<>();
                for (ApkItem item : rawItems) {
                    String pkg = item.getPackageName();
                    if (pkg == null || pkg.isEmpty() || pkg.equals("unknown")) {
                        pkg = "unknown." + item.getFileName();
                    }

                    ApkGroup group = groupMap.get(pkg);
                    if (group == null) {
                        group = new ApkGroup(pkg, item.getAppName(), item.getIcon());
                        group.setInstalledInfo(item.isInstalled(), item.getInstalledVersionName(), item.getInstalledVersionCode());
                        groupMap.put(pkg, group);
                    }
                    group.addItem(item);
                }

                List<ApkGroup> groupList = new ArrayList<>(groupMap.values());

                for (ApkGroup group : groupList) {
                    Collections.sort(group.getItems(), new Comparator<ApkItem>() {
                        @Override
                        public int compare(ApkItem o1, ApkItem o2) {
                            return Long.compare(o2.getVersionCode(), o1.getVersionCode());
                        }
                    });

                    if (!group.getItems().isEmpty()) {
                        group.getItems().get(0).setLatest(true);
                        for (int i = 1; i < group.getItems().size(); i++) {
                            group.getItems().get(i).setLatest(false);
                        }
                    }
                }

                Collections.sort(groupList, new Comparator<ApkGroup>() {
                    @Override
                    public int compare(ApkGroup g1, ApkGroup g2) {
                        return g1.getAppName().compareToIgnoreCase(g2.getAppName());
                    }
                });

                final List<ApkGroup> finalGroupList = groupList;
                final List<ApkItem> finalRawItems = rawItems;

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onComplete(finalGroupList, finalRawItems);
                        }
                    }
                });
            }
        }).start();
    }

    private static void scanRecursive(PackageManager pm, File dir, List<ApkItem> resultList, ScanCallback callback, Handler handler) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (final File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();
                if (name.startsWith(".") || name.equalsIgnoreCase("Android")) {
                    continue;
                }
                scanRecursive(pm, file, resultList, callback, handler);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".apk")) {
                final int currentCount = resultList.size() + 1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onProgress(file.getAbsolutePath(), currentCount);
                        }
                    }
                });

                ApkItem item = parseApk(pm, file);
                if (item != null) {
                    resultList.add(item);
                }
            }
        }
    }

    private static ApkItem parseApk(PackageManager pm, File file) {
        String path = file.getAbsolutePath();
        try {
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(path, 0);
            if (pkgInfo != null) {
                pkgInfo.applicationInfo.sourceDir = path;
                pkgInfo.applicationInfo.publicSourceDir = path;

                CharSequence appNameSeq = pkgInfo.applicationInfo.loadLabel(pm);
                String appName = appNameSeq != null ? appNameSeq.toString() : file.getName();
                Drawable icon = null;
                try {
                    icon = pkgInfo.applicationInfo.loadIcon(pm);
                } catch (Exception ignored) {}

                String versionName = pkgInfo.versionName != null ? pkgInfo.versionName : "1.0";
                long versionCode;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    versionCode = pkgInfo.getLongVersionCode();
                } else {
                    versionCode = pkgInfo.versionCode;
                }

                boolean isInstalled = false;
                String installedVersionName = "";
                long installedVersionCode = -1;

                try {
                    PackageInfo installedInfo = pm.getPackageInfo(pkgInfo.packageName, 0);
                    if (installedInfo != null) {
                        isInstalled = true;
                        installedVersionName = installedInfo.versionName != null ? installedInfo.versionName : "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            installedVersionCode = installedInfo.getLongVersionCode();
                        } else {
                            installedVersionCode = installedInfo.versionCode;
                        }
                    }
                } catch (PackageManager.NameNotFoundException ignored) {}

                ApkItem item = new ApkItem(
                        path,
                        file.getName(),
                        pkgInfo.packageName,
                        appName,
                        versionName,
                        versionCode,
                        file.length(),
                        file.lastModified(),
                        icon
                );
                item.setInstalledInfo(isInstalled, installedVersionName, installedVersionCode);
                return item;
            }
        } catch (Exception ignored) {}

        return new ApkItem(
                path,
                file.getName(),
                "unknown",
                file.getName(),
                "לא ידוע",
                0,
                file.length(),
                file.lastModified(),
                null
        );
    }
}
