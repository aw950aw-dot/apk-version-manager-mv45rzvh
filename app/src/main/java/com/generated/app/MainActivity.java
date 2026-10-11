package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ApkAdapter.OnSelectionChangedListener {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvTotalFiles;
    private TextView tvTotalSize;
    private TextView tvOldVersionsCount;

    private LinearLayout layoutScanning;
    private TextView tvScanProgress;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private ListView listViewApks;

    private LinearLayout layoutBottomBar;
    private TextView tvSelectedCount;
    private Button btnToggleSelectAll;
    private Button btnCopy;
    private Button btnMove;
    private Button btnDelete;

    private Button btnScanAll;
    private Button btnFilterDuplicates;
    private Button btnScanFolder;
    private Button btnScanSdCard;
    private Button btnSelectOld;

    private ApkAdapter adapter;
    private List<ApkGroup> currentGroups = new ArrayList<>();
    private List<ApkItem> allScanItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();

        if (checkAndRequestPermissions()) {
            startScan(null);
        }
    }

    private void initViews() {
        tvTotalFiles = findViewById(R.id.tvTotalFiles);
        tvTotalSize = findViewById(R.id.tvTotalSize);
        tvOldVersionsCount = findViewById(R.id.tvOldVersionsCount);

        layoutScanning = findViewById(R.id.layoutScanning);
        tvScanProgress = findViewById(R.id.tvScanProgress);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        listViewApks = findViewById(R.id.listViewApks);

        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnToggleSelectAll = findViewById(R.id.btnToggleSelectAll);
        btnCopy = findViewById(R.id.btnCopy);
        btnMove = findViewById(R.id.btnMove);
        btnDelete = findViewById(R.id.btnDelete);

        btnScanAll = findViewById(R.id.btnScanAll);
        btnFilterDuplicates = findViewById(R.id.btnFilterDuplicates);
        btnScanFolder = findViewById(R.id.btnScanFolder);
        btnScanSdCard = findViewById(R.id.btnScanSdCard);
        btnSelectOld = findViewById(R.id.btnSelectOld);

        adapter = new ApkAdapter(this, this);
        listViewApks.setAdapter(adapter);
    }

    private void setupListeners() {
        btnScanAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    startScan(null);
                }
            }
        });

        btnFilterDuplicates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean active = !adapter.isDuplicatesOnlyFilter();
                adapter.setDuplicatesOnlyFilter(active);
                if (active) {
                    btnFilterDuplicates.setText(R.string.show_all_apks);
                    btnFilterDuplicates.setBackgroundResource(R.drawable.bg_button_primary);
                } else {
                    btnFilterDuplicates.setText(R.string.filter_duplicates_only);
                    btnFilterDuplicates.setBackgroundResource(R.drawable.bg_button_secondary);
                }
                updateListViewVisibility();
                onSelectionChanged();
            }
        });

        btnScanFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    new FolderPickerDialog(MainActivity.this, new FolderPickerDialog.OnFolderSelectedListener() {
                        @Override
                        public void onFolderSelected(File folder) {
                            startScan(folder);
                        }
                    }).show();
                }
            }
        });

        btnScanSdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    StorageUtils.StorageVolume sdCard = StorageUtils.getFirstSdCard(MainActivity.this);
                    if (sdCard != null) {
                        startScan(sdCard.getRootPath());
                    } else {
                        Toast.makeText(MainActivity.this, R.string.sd_card_not_found, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnSelectOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.selectAllOld();
            }
        });

        btnToggleSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ApkItem> selected = adapter.getSelectedItems();
                if (selected.size() == allScanItems.size()) {
                    adapter.setAllSelected(false);
                } else {
                    adapter.setAllSelected(true);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteSelected();
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDestinationAndProcess(false);
            }
        });

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDestinationAndProcess(true);
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title)
                        .setMessage(R.string.permission_msg)
                        .setPositiveButton(R.string.grant_permission, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return false;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private void startScan(File targetFolder) {
        layoutScanning.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        listViewApks.setVisibility(View.GONE);

        List<File> dirsToScan = new ArrayList<>();
        if (targetFolder != null) {
            dirsToScan.add(targetFolder);
        } else {
            dirsToScan.addAll(StorageUtils.getAllStorageRoots(this));
        }

        ApkScanner.scanDirectories(this, dirsToScan, new ApkScanner.ScanCallback() {
            @Override
            public void onProgress(String currentPath, int count) {
                tvScanProgress.setText("סורק: נמצאו " + count + " קבצים...");
            }

            @Override
            public void onComplete(List<ApkGroup> groups, List<ApkItem> allItems) {
                layoutScanning.setVisibility(View.GONE);
                currentGroups = groups;
                allScanItems = allItems;

                adapter.updateData(groups);

                updateListViewVisibility();
                updateStats();
                onSelectionChanged();
            }
        });
    }

    private void updateListViewVisibility() {
        if (allScanItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(R.string.no_apks_found);
            listViewApks.setVisibility(View.GONE);
        } else if (adapter.getCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(R.string.no_duplicates_found);
            listViewApks.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            listViewApks.setVisibility(View.VISIBLE);
        }
    }

    private void updateStats() {
        int totalFiles = allScanItems.size();
        long totalBytes = 0;
        int oldCount = 0;

        for (ApkItem item : allScanItems) {
            totalBytes += item.getFileSize();
            if (!item.isLatest()) {
                oldCount++;
            }
        }

        tvTotalFiles.setText(String.valueOf(totalFiles));
        tvTotalSize.setText(FileUtils.formatSize(totalBytes));
        tvOldVersionsCount.setText(String.valueOf(oldCount));
    }

    @Override
    public void onSelectionChanged() {
        List<ApkItem> selected = adapter.getSelectedItems();
        int count = selected.size();

        if (count > 0) {
            layoutBottomBar.setVisibility(View.VISIBLE);
            tvSelectedCount.setText("נבחרו " + count + " קבצים");
            if (count == allScanItems.size() && !allScanItems.isEmpty()) {
                btnToggleSelectAll.setText(R.string.deselect_all);
            } else {
                btnToggleSelectAll.setText(R.string.select_all);
            }
        } else {
            layoutBottomBar.setVisibility(View.GONE);
        }
    }

    private void confirmDeleteSelected() {
        final List<ApkItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage("האם אתה בטוח שברצונך למחוק " + selected.size() + " קבצי APK?")
                .setPositiveButton(R.string.delete_selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performDelete(selected);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDelete(final List<ApkItem> items) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("מוחק קבצים...");
        pd.setCancelable(false);
        pd.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int deletedCount = 0;
                for (ApkItem item : items) {
                    File file = new File(item.getFilePath());
                    if (file.exists() && file.delete()) {
                        deletedCount++;
                    }
                }

                final int result = deletedCount;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "נמחקו " + result + " קבצים בהצלחה", Toast.LENGTH_SHORT).show();
                        startScan(null);
                    }
                });
            }
        }).start();
    }

    private void pickDestinationAndProcess(final boolean isMove) {
        final List<ApkItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) return;

        new FolderPickerDialog(this, new FolderPickerDialog.OnFolderSelectedListener() {
            @Override
            public void onFolderSelected(File destinationFolder) {
                performCopyOrMove(selected, destinationFolder, isMove);
            }
        }).show();
    }

    private void performCopyOrMove(final List<ApkItem> items, final File destFolder, final boolean isMove) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage((isMove ? "מעביר" : "מעתיק") + " קבצים...");
        pd.setCancelable(false);
        pd.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int successCount = 0;
                for (ApkItem item : items) {
                    File src = new File(item.getFilePath());
                    File dst = new File(destFolder, src.getName());

                    boolean ok;
                    if (isMove) {
                        ok = FileUtils.moveFile(src, dst);
                    } else {
                        ok = FileUtils.copyFile(src, dst);
                    }

                    if (ok) successCount++;
                }

                final int result = successCount;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        String actionStr = isMove ? "הועברו" : "הועתקו";
                        Toast.makeText(MainActivity.this, actionStr + " " + result + " קבצים בהצלחה", Toast.LENGTH_SHORT).show();
                        startScan(null);
                    }
                });
            }
        }).start();
    }
}
