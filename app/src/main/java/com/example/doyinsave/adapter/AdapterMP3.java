package com.example.doyinsave.adapter;

import static com.example.doyinsave.utils.FileHelper.getMp3FilesFromFolder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.example.doyinsave.DownloadActivity;
import com.example.doyinsave.MainActivity;
import com.example.doyinsave.MusicActivity;
import com.example.doyinsave.R;
import com.example.doyinsave.model.MP3model;
import com.example.doyinsave.utils.FileHelper;

import java.io.File;
import java.util.List;

public class AdapterMP3 extends BaseAdapter {
    private Context context;
    private List<MP3model> list;

    public AdapterMP3(Context context, List<MP3model> list) {
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
            view = inflater.inflate(R.layout.item_mp3, viewGroup, false);

            // Tạo một ViewHolder để lưu trữ các thành phần của layout
            holder = new ViewHolder();
            holder.tvNameFile = view.findViewById(R.id.tv_name_music);
            holder.tvSizeFile = view.findViewById(R.id.tv_sizefile);
            holder.imgMenu = view.findViewById(R.id.img_menu);
            holder.btnStart = view.findViewById(R.id.btn_start_music);
            view.setTag(holder);
        } else {
            // Nếu convertView được tái sử dụng, sử dụng ViewHolder đã lưu trữ trước đó
            holder = (ViewHolder) view.getTag();
        }

        // Thiết lập dữ liệu cho các thành phần của layout từ danh sách dữ liệu
        MP3model item = list.get(i);
        String title = item.getTitle();
        int maxLength = 20; // Đặt độ dài tối đa bạn muốn hiển thị
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
                        shareFile(item.getTitle(), item.getParent());
                        return true;
                    case R.id.delete:
                        deleteFile(item.getTitle(), item.getParent());
                        return true;
                }
                return false;
            });
            popupMenu.show();
        });
        holder.btnStart.setOnClickListener(v -> {
            playAudio(item.getTitle(), item.getParent());
            onItemClicked1(item.getPath());
        });
        return view;
    }

    // ViewHolder để lưu trữ các thành phần của layout và tránh việc gọi findViewById nhiều lần
    static class ViewHolder {
        TextView tvNameFile, tvSizeFile;
        ImageView imgMenu;
        ConstraintLayout btnStart;
    }

    private void deleteFile(String nameFile, String path) {
        File file = new File(path, nameFile);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.e("Delete1", "File đã được xóa");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.removeIf(mp3model -> mp3model.getTitle().equals(nameFile) && mp3model.getParent().equals(path));
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

    private void playAudio(String nameFile, String path) {
        File file = new File(path, nameFile);
        if (file.exists()) {
//            Uri audioUri = Uri.fromFile(file);
//            Intent audioIntent = new Intent(Intent.ACTION_VIEW);
//            audioIntent.setDataAndType(audioUri, "audio/*");
//            audioIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            try {
//                context.startActivity(audioIntent);
//            } catch (Exception e) {
//                Log.e("Music", "Không có ứng dụng hỗ trợ nghe nhạc");
//            }
//            onItemClicked(file);
        } else {
            Log.e("Music", "File âm thanh không tồn tại");
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
        intent.putExtra("typeFile",2);
        intent.putExtra("pathFile",path);
        context.startActivity(intent);
    }
}
