package com.generated.app;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageUtils {

    public static class StorageVolume {
        private String title;
        private File rootPath;
        private boolean isRemovable;

        public StorageVolume(String title, File rootPath, boolean isRemovable) {
            this.title = title;
            this.rootPath = rootPath;
            this.isRemovable = isRemovable;
        }

        public String getTitle() {
            return title;
        }

        public File getRootPath() {
            return rootPath;
        }

        public boolean isRemovable() {
            return isRemovable;
        }

        public String getFreeSpaceDescription() {
            try {
                long free = rootPath.getFreeSpace();
                long total = rootPath.getTotalSpace();
                if (total > 0) {
                    return FileUtils.formatSize(free) + " פנוי";
                }
            } catch (Exception ignored) {}
            return "";
        }
    }

    public static List<StorageVolume> getStorageVolumes(Context context) {
        List<StorageVolume> volumes = new ArrayList<>();
        Set<String> addedPaths = new HashSet<>();

        // 1. Primary Internal Storage
        File internalStorage = Environment.getExternalStorageDirectory();
        if (internalStorage != null && internalStorage.exists()) {
            volumes.add(new StorageVolume(
                    context.getString(R.string.internal_storage),
                    internalStorage,
                    false
            ));
            try {
                addedPaths.add(internalStorage.getCanonicalPath());
            } catch (Exception e) {
                addedPaths.add(internalStorage.getAbsolutePath());
            }
        }

        // 2. Discover via getExternalFilesDirs (Framework method to detect secondary SD cards)
        try {
            File[] extDirs = context.getExternalFilesDirs(null);
            if (extDirs != null) {
                int sdCardIndex = 1;
                for (File ext : extDirs) {
                    if (ext == null) continue;
                    File root = extractVolumeRoot(ext);
                    if (root != null && root.exists() && root.canRead()) {
                        String canonical;
                        try {
                            canonical = root.getCanonicalPath();
                        } catch (Exception e) {
                            canonical = root.getAbsolutePath();
                        }

                        if (!addedPaths.contains(canonical)) {
                            addedPaths.add(canonical);
                            boolean isRemovable = true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                try {
                                    isRemovable = Environment.isExternalStorageRemovable(root);
                                } catch (Exception ignored) {}
                            }
                            String label = context.getString(R.string.sd_card);
                            if (sdCardIndex > 1) {
                                label += " " + sdCardIndex;
                            }
                            volumes.add(new StorageVolume(label, root, isRemovable));
                            sdCardIndex++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // 3. Inspect /storage directory directly for mounted removable media
        File storageDir = new File("/storage");
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f != null && f.isDirectory() && f.canRead()) {
                        String name = f.getName().toLowerCase();
                        if (name.equals("self") || name.equals("emulated") || name.equals("knox")) {
                            continue;
                        }
                        String canonical;
                        try {
                            canonical = f.getCanonicalPath();
                        } catch (Exception e) {
                            canonical = f.getAbsolutePath();
                        }

                        if (!addedPaths.contains(canonical)) {
                            addedPaths.add(canonical);
                            String label = context.getString(R.string.sd_card);
                            if (volumes.size() > 1) {
                                label += " (" + f.getName() + ")";
                            }
                            volumes.add(new StorageVolume(label, f, true));
                        }
                    }
                }
            }
        }

        return volumes;
    }

    private static File extractVolumeRoot(File file) {
        String path = file.getAbsolutePath();
        int androidIdx = path.indexOf("/Android/data");
        if (androidIdx > 0) {
            return new File(path.substring(0, androidIdx));
        }

        File cur = file;
        while (cur != null && cur.getParentFile() != null) {
            File parent = cur.getParentFile();
            if ("/storage".equals(parent.getAbsolutePath()) || "/mnt".equals(parent.getAbsolutePath())) {
                return cur;
            }
            cur = parent;
        }
        return file;
    }

    public static List<File> getAllStorageRoots(Context context) {
        List<StorageVolume> volumes = getStorageVolumes(context);
        List<File> roots = new ArrayList<>();
        for (StorageVolume v : volumes) {
            roots.add(v.getRootPath());
        }
        return roots;
    }

    public static StorageVolume getFirstSdCard(Context context) {
        List<StorageVolume> volumes = getStorageVolumes(context);
        for (StorageVolume v : volumes) {
            if (v.isRemovable()) {
                return v;
            }
        }
        return null;
    }
}
