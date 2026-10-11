package com.generated.app;

import android.graphics.drawable.Drawable;

public class ApkItem {
    public static final int INSTALL_STATUS_NOT_INSTALLED = 0;
    public static final int INSTALL_STATUS_NEWER = 1;
    public static final int INSTALL_STATUS_OLDER = 2;
    public static final int INSTALL_STATUS_SAME = 3;

    private String filePath;
    private String fileName;
    private String packageName;
    private String appName;
    private String versionName;
    private long versionCode;
    private long fileSize;
    private long lastModified;
    private Drawable icon;
    private boolean isLatest;
    private boolean isSelected;

    private boolean isInstalled;
    private String installedVersionName;
    private long installedVersionCode;

    public ApkItem(String filePath, String fileName, String packageName, String appName,
                   String versionName, long versionCode, long fileSize, long lastModified, Drawable icon) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.packageName = packageName;
        this.appName = appName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.icon = icon;
        this.isLatest = false;
        this.isSelected = false;
        this.isInstalled = false;
        this.installedVersionName = "";
        this.installedVersionCode = -1;
    }

    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public String getVersionName() { return versionName; }
    public long getVersionCode() { return versionCode; }
    public long getFileSize() { return fileSize; }
    public long getLastModified() { return lastModified; }
    public Drawable getIcon() { return icon; }

    public boolean isLatest() { return isLatest; }
    public void setLatest(boolean latest) { isLatest = latest; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isInstalled() { return isInstalled; }
    public String getInstalledVersionName() { return installedVersionName; }
    public long getInstalledVersionCode() { return installedVersionCode; }

    public void setInstalledInfo(boolean installed, String installedVersionName, long installedVersionCode) {
        this.isInstalled = installed;
        this.installedVersionName = installedVersionName != null ? installedVersionName : "";
        this.installedVersionCode = installedVersionCode;
    }

    public int getInstallComparisonStatus() {
        if (!isInstalled) {
            return INSTALL_STATUS_NOT_INSTALLED;
        }
        if (installedVersionCode > 0 && versionCode > 0) {
            if (versionCode > installedVersionCode) return INSTALL_STATUS_NEWER;
            if (versionCode < installedVersionCode) return INSTALL_STATUS_OLDER;
        }
        // Fallback to version name comparison
        if (versionName != null && installedVersionName != null && !versionName.equals(installedVersionName)) {
            int cmp = compareVersions(versionName, installedVersionName);
            if (cmp > 0) return INSTALL_STATUS_NEWER;
            if (cmp < 0) return INSTALL_STATUS_OLDER;
        }
        return INSTALL_STATUS_SAME;
    }

    private int compareVersions(String v1, String v2) {
        try {
            String[] parts1 = v1.replaceAll("[^0-9.]", "").split("\\.");
            String[] parts2 = v2.replaceAll("[^0-9.]", "").split("\\.");
            int length = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < length; i++) {
                int p1 = i < parts1.length && !parts1[i].isEmpty() ? Integer.parseInt(parts1[i]) : 0;
                int p2 = i < parts2.length && !parts2[i].isEmpty() ? Integer.parseInt(parts2[i]) : 0;
                if (p1 > p2) return 1;
                if (p1 < p2) return -1;
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
