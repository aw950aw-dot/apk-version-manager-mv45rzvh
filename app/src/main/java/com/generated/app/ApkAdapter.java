package com.generated.app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ApkAdapter extends BaseAdapter {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public static class DisplayItem {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_APK = 1;

        public int type;
        public ApkGroup group;
        public ApkItem apkItem;

        public DisplayItem(ApkGroup group) {
            this.type = TYPE_HEADER;
            this.group = group;
        }

        public DisplayItem(ApkItem apkItem) {
            this.type = TYPE_APK;
            this.apkItem = apkItem;
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private List<DisplayItem> displayItems;
    private OnSelectionChangedListener selectionListener;

    public ApkAdapter(Context context, OnSelectionChangedListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.displayItems = new ArrayList<>();
        this.selectionListener = listener;
    }

    public void updateData(List<ApkGroup> groups) {
        displayItems.clear();
        for (ApkGroup group : groups) {
            displayItems.add(new DisplayItem(group));
            for (ApkItem item : group.getItems()) {
                displayItems.add(new DisplayItem(item));
            }
        }
        notifyDataSetChanged();
    }

    public void selectAllOld() {
        for (DisplayItem di : displayItems) {
            if (di.type == DisplayItem.TYPE_APK && di.apkItem != null) {
                di.apkItem.setSelected(!di.apkItem.isLatest());
            }
        }
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged();
    }

    public void setAllSelected(boolean selected) {
        for (DisplayItem di : displayItems) {
            if (di.type == DisplayItem.TYPE_APK && di.apkItem != null) {
                di.apkItem.setSelected(selected);
            }
        }
        notifyDataSetChanged();
        if (selectionListener != null) selectionListener.onSelectionChanged();
    }

    public List<ApkItem> getSelectedItems() {
        List<ApkItem> list = new ArrayList<>();
        for (DisplayItem di : displayItems) {
            if (di.type == DisplayItem.TYPE_APK && di.apkItem != null && di.apkItem.isSelected()) {
                list.add(di.apkItem);
            }
        }
        return list;
    }

    @Override
    public int getCount() {
        return displayItems.size();
    }

    @Override
    public Object getItem(int position) {
        return displayItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DisplayItem item = displayItems.get(position);

        if (item.type == DisplayItem.TYPE_HEADER) {
            HeaderViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_apk_group_header, parent, false);
                holder = new HeaderViewHolder();
                holder.imgIcon = convertView.findViewById(R.id.imgGroupIcon);
                holder.tvAppName = convertView.findViewById(R.id.tvGroupAppName);
                holder.tvPackage = convertView.findViewById(R.id.tvGroupPackage);
                holder.tvCount = convertView.findViewById(R.id.tvGroupCount);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            ApkGroup group = item.group;
            if (group.getMainIcon() != null) {
                holder.imgIcon.setImageDrawable(group.getMainIcon());
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_apk);
            }

            holder.tvAppName.setText(group.getAppName());
            holder.tvPackage.setText(group.getPackageName());
            holder.tvCount.setText(group.getItems().size() + " גרסאות");

            return convertView;
        } else {
            ApkViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_apk_file, parent, false);
                holder = new ApkViewHolder();
                holder.cbSelect = convertView.findViewById(R.id.cbSelect);
                holder.tvVersion = convertView.findViewById(R.id.tvVersion);
                holder.tvBadge = convertView.findViewById(R.id.tvBadge);
                holder.tvMeta = convertView.findViewById(R.id.tvMeta);
                holder.tvPath = convertView.findViewById(R.id.tvPath);
                convertView.setTag(holder);
            } else {
                holder = (ApkViewHolder) convertView.getTag();
            }

            final ApkItem apkItem = item.apkItem;

            holder.tvVersion.setText("גרסה " + apkItem.getVersionName() + " (" + apkItem.getVersionCode() + ")");
            holder.tvMeta.setText(FileUtils.formatSize(apkItem.getFileSize()) + " • " + FileUtils.formatDate(apkItem.getLastModified()));
            holder.tvPath.setText(apkItem.getFilePath());

            if (apkItem.isLatest()) {
                holder.tvBadge.setText(context.getString(R.string.latest_badge));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_new);
                holder.tvBadge.setTextColor(context.getResources().getColor(R.color.badge_new_text));
            } else {
                holder.tvBadge.setText(context.getString(R.string.old_badge));
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_old);
                holder.tvBadge.setTextColor(context.getResources().getColor(R.color.badge_old_text));
            }

            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(apkItem.isSelected());

            holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    apkItem.setSelected(isChecked);
                    if (selectionListener != null) selectionListener.onSelectionChanged();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    apkItem.setSelected(!apkItem.isSelected());
                    notifyDataSetChanged();
                    if (selectionListener != null) selectionListener.onSelectionChanged();
                }
            });

            return convertView;
        }
    }

    private static class HeaderViewHolder {
        ImageView imgIcon;
        TextView tvAppName;
        TextView tvPackage;
        TextView tvCount;
    }

    private static class ApkViewHolder {
        CheckBox cbSelect;
        TextView tvVersion;
        TextView tvBadge;
        TextView tvMeta;
        TextView tvPath;
    }
}
