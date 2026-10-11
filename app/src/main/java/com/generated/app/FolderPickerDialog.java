package com.generated.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderPickerDialog {

    public interface OnFolderSelectedListener {
        void onFolderSelected(File folder);
    }

    private Context context;
    private File currentDir;
    private StorageUtils.StorageVolume currentVolume;
    private List<StorageUtils.StorageVolume> volumes;
    private OnFolderSelectedListener listener;
    private AlertDialog dialog;
    private FolderAdapter adapter;
    private TextView tvPath;
    private LinearLayout layoutVolumeChips;

    public FolderPickerDialog(Context context, OnFolderSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.volumes = StorageUtils.getStorageVolumes(context);

        if (!volumes.isEmpty()) {
            this.currentVolume = volumes.get(0);
            this.currentDir = currentVolume.getRootPath();
        } else {
            this.currentDir = android.os.Environment.getExternalStorageDirectory();
        }
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_folder_picker, null);
        builder.setView(view);

        tvPath = view.findViewById(R.id.tvPickerCurrentPath);
        ListView listView = view.findViewById(R.id.listViewFolders);
        Button btnNewFolder = view.findViewById(R.id.btnNewFolder);
        Button btnSelect = view.findViewById(R.id.btnSelectFolder);
        layoutVolumeChips = view.findViewById(R.id.layoutVolumeChips);

        setupVolumeChips();

        adapter = new FolderAdapter();
        listView.setAdapter(adapter);

        updateDirectory(currentDir);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = adapter.getItem(position);
                if (file.getName().equals("..")) {
                    File parentDir = currentDir.getParentFile();
                    if (parentDir != null && isUnderVolume(parentDir)) {
                        updateDirectory(parentDir);
                    }
                } else {
                    updateDirectory(file);
                }
            }
        });

        btnNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateFolderDialog();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onFolderSelected(currentDir);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        dialog = builder.create();
        dialog.show();
    }

    private void setupVolumeChips() {
        layoutVolumeChips.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (final StorageUtils.StorageVolume volume : volumes) {
            View chipView = inflater.inflate(R.layout.item_folder_volume_chip, layoutVolumeChips, false);
            ImageView imgIcon = chipView.findViewById(R.id.imgChipIcon);
            TextView tvTitle = chipView.findViewById(R.id.tvChipTitle);
            TextView tvSub = chipView.findViewById(R.id.tvChipSubtitle);

            tvTitle.setText(volume.getTitle());
            tvSub.setText(volume.getFreeSpaceDescription());

            if (volume.isRemovable()) {
                imgIcon.setImageResource(R.drawable.ic_sd_card);
            } else {
                imgIcon.setImageResource(R.drawable.ic_phone_storage);
            }

            updateChipState(chipView, volume.equals(currentVolume));

            chipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentVolume = volume;
                    updateVolumeChipsSelection();
                    updateDirectory(volume.getRootPath());
                }
            });

            chipView.setTag(volume);
            layoutVolumeChips.addView(chipView);
        }
    }

    private void updateVolumeChipsSelection() {
        for (int i = 0; i < layoutVolumeChips.getChildCount(); i++) {
            View chip = layoutVolumeChips.getChildAt(i);
            Object tag = chip.getTag();
            if (tag instanceof StorageUtils.StorageVolume) {
                updateChipState(chip, tag.equals(currentVolume));
            }
        }
    }

    private void updateChipState(View chip, boolean isActive) {
        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_volume_chip_active);
        } else {
            chip.setBackgroundResource(R.drawable.bg_volume_chip_inactive);
        }
    }

    private boolean isUnderVolume(File file) {
        if (currentVolume == null || file == null) return false;
        try {
            String volPath = currentVolume.getRootPath().getCanonicalPath();
            String filePath = file.getCanonicalPath();
            return filePath.startsWith(volPath);
        } catch (Exception e) {
            return file.getAbsolutePath().startsWith(currentVolume.getRootPath().getAbsolutePath());
        }
    }

    private boolean isAtVolumeRoot(File dir) {
        if (currentVolume == null || dir == null) return false;
        try {
            return dir.getCanonicalPath().equals(currentVolume.getRootPath().getCanonicalPath());
        } catch (Exception e) {
            return dir.getAbsolutePath().equals(currentVolume.getRootPath().getAbsolutePath());
        }
    }

    private void updateDirectory(File dir) {
        if (dir == null || !dir.isDirectory()) return;
        currentDir = dir;
        tvPath.setText(currentDir.getAbsolutePath());

        List<File> folders = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    folders.add(f);
                }
            }
        }

        Collections.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        // Add parent directory navigation if not at volume root
        if (!isAtVolumeRoot(currentDir) && currentDir.getParentFile() != null && isUnderVolume(currentDir.getParentFile())) {
            folders.add(0, new File(currentDir.getParentFile(), ".."));
        }

        adapter.setFolders(folders);
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.new_folder);

        final EditText input = new EditText(context);
        input.setHint(R.string.folder_name);
        builder.setView(input);

        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    File newDir = new File(currentDir, name);
                    if (newDir.mkdir() || newDir.exists()) {
                        updateDirectory(newDir);
                    } else {
                        Toast.makeText(context, "שגיאה ביצירת תיקייה", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private class FolderAdapter extends BaseAdapter {
        private List<File> folders = new ArrayList<>();

        public void setFolders(List<File> list) {
            this.folders = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() { return folders.size(); }

        @Override
        public File getItem(int position) { return folders.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_folder_picker, parent, false);
            }

            File file = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tvFolderName);
            ImageView imgIcon = convertView.findViewById(R.id.imgFolderIcon);

            if (file.getName().equals("..")) {
                tvName.setText(".. (חזור תיקייה אחת למעלה)");
                imgIcon.setImageResource(R.drawable.ic_folder);
            } else {
                tvName.setText(file.getName());
                imgIcon.setImageResource(R.drawable.ic_folder);
            }

            return convertView;
        }
    }
}
