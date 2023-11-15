package com.example.doyinsave.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.example.doyinsave.MusicActivity;
import com.example.doyinsave.R;
import com.example.doyinsave.model.MP4model;
import com.example.doyinsave.utils.FileHelper;

import java.io.File;
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
            holder.btnStartVideo = view.findViewById(R.id.btn_startVideo);
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
                        shareFile(item.getTitle(),item.getParent());

                        return true;
                    case R.id.delete:
                        deleteFile(item.getTitle(), item.getParent());
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
        holder.btnStartVideo.setOnClickListener(v -> {
            startVideo(item.getTitle(),item.getParent());
            onItemClicked1(item.getPath());
        });
        return view;
    }

    static class ViewHolder {
        TextView tvNameFile, tvSizeFile;
        ImageView imgMenu;
        RelativeLayout btnStartVideo;
    }
    private void deleteFile(String nameFile,  String path) {
        File file = new File(path, nameFile);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.e("Delete1", "File đã được xóa");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.removeIf(mp4model -> mp4model.getTitle().equals(nameFile) && mp4model.getParent().equals(path));
                }
                notifyDataSetChanged();
            } else {
                Log.e("Delete1", "Không thể xóa file");
            }
        } else {
            Log.e("Delete1", "File không tồn tại");
        }
    }
    private void shareFile(String nameFile, String path) {
        File file = new File(path, nameFile);
        if (file.exists()) {
            Uri fileUri = Uri.fromFile(file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ file qua"));

        } else {
            Log.e("Share1", "File không tồn tại");
        }
    }
    private void startVideo(String fileName,String folderPath){
        File file = new File(folderPath, fileName);
        if (file.exists()) {
//            Uri videoUri = Uri.fromFile(file);
//            Intent videoIntent = new Intent(Intent.ACTION_VIEW);
//            videoIntent.setDataAndType(videoUri, "video/*");
//            videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            try {
//                // Mở trình xem video
//                context.startActivity(videoIntent);
//            } catch (Exception e) {
//                Log.e("PlayVideo", "Không có ứng dụng hỗ trợ xem video");
//            }
//            onItemClicked(file);

        } else {
            Log.e("PlayVideo", "File video không tồn tại");
        }
    }
    public void onItemClicked(File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, context.getContentResolver().getType(uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }
    public void onItemClicked1(String path){
        Intent intent = new Intent(context, MusicActivity.class);
        intent.putExtra("typeFile",1);
        intent.putExtra("pathFile",path);
        context.startActivity(intent);
    }
}
