package com.example.doyinsave.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.doyinsave.R;
import com.example.doyinsave.model.MP4model;
import com.example.doyinsave.utils.FileHelper;

import java.util.List;

public class AdapterMP4 extends BaseAdapter {
    private Context context;
    private List<MP4model> list;

    public AdapterMP4(Context context, List<MP4model> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            // Nếu convertView không được tái sử dụng, inflate một layout mới
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_video, viewGroup, false);

            // Tạo một ViewHolder để lưu trữ các thành phần của layout
            holder = new ViewHolder();
            holder.tvNameFile = view.findViewById(R.id.tv_name);
            holder.tvSizeFile = view.findViewById(R.id.tv_size);
            holder.imgMenu = view.findViewById(R.id.img_menu);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        MP4model item = list.get(i);
        String title = item.getTitle();
        int maxLength = 10;
        if (title.length() > maxLength) {
            title = title.substring(0, maxLength) + "...";
        }
        holder.tvNameFile.setText(title);
        holder.tvSizeFile.setText(FileHelper.getFileSize(item.getPath()));
        holder.imgMenu.setOnClickListener(view1 -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.imgMenu);
            popupMenu.getMenuInflater().inflate(R.menu.menu_click_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.share:


                        return true;
                    case R.id.delete:

                        return true;
                }
                return false;
            });
        });
        return view;
    }

    static class ViewHolder {
        TextView tvNameFile, tvSizeFile;
        ImageView imgMenu;
    }
}
