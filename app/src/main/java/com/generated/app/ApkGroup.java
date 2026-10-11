package com.generated.app;

import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import java.util.List;

public class ApkGroup {
    private String packageName;
    private String appName;
    private Drawable mainIcon;
    private List<ApkItem> items;

    public ApkGroup(String packageName, String appName, Drawable mainIcon) {
        this.packageName = packageName;
        this.appName = appName;
        this.mainIcon = mainIcon;
        this.items = new ArrayList<>();
    }

    public String getPackageName() { return packageName; }
    public String getAppName() { return appName; }
    public Drawable getMainIcon() { return mainIcon; }
    public List<ApkItem> getItems() { return items; }

    public void addItem(ApkItem item) {
        items.add(item);
    }
}
