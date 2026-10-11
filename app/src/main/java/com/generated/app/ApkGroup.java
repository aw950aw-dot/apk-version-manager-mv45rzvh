package com.generated.app;

import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

public class ApkGroup {
    private String packageName;
    private String appName;
    private Drawable mainIcon;
    private List<ApkItem> items;

    private boolean isInstalled;
    private String installedVersionName;
    private long installedVersionCode;

    public ApkGroup(String packageName, String appName, Drawable mainIcon) {
        this.packageName = packageName;
        this.appName = appName;
        this.mainIcon = mainIcon;
        this.items = new ArrayList<>();
        this.isInstalled = false;
        this.installedVersionName = "";
        this.installedVersionCode = -1;
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getMainIcon() { return mainIcon; }
    public List<ApkItem> getItems() { return items; }

    public void addItem(ApkItem item) {
        items.add(item);
    }

    public boolean isInstalled() { return isInstalled; }
    public String getInstalledVersionName() { return installedVersionName; }
    public long getInstalledVersionCode() { return installedVersionCode; }

    public void setInstalledInfo(boolean installed, String installedVersionName, long installedVersionCode) {
        this.isInstalled = installed;
        this.installedVersionName = installedVersionName != null ? installedVersionName : "";
        this.installedVersionCode = installedVersionCode;
    }
}
