package com.generated.app;

import android.graphics.drawable.Drawable;

public class ApkItem {
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
}
