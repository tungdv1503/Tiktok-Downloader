package com.example.doyinsave.Fragment;

import static com.example.doyinsave.utils.FileHelper.getMp3FilesFromFolder;
import static com.example.doyinsave.utils.FileHelper.getMp4FilesFromFolder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.doyinsave.MusicActivity;
import com.example.doyinsave.R;
import com.example.doyinsave.adapter.AdapterMP3;
import com.example.doyinsave.model.MP3model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Mp3Fragment extends Fragment {
    TextView tvNoFile;
    ImageView imgNoFile;
    ListView lvDanhSach;
    ArrayList<MP3model> list;
    AdapterMP3 adapterMP3;

    public Mp3Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mp3, container, false);
        initViews(v);
        listener(v);
        return v;
    }

    private void initViews(View v) {
        tvNoFile = v.findViewById(R.id.no_file_download);
        imgNoFile = v.findViewById(R.id.img_nofile);
        lvDanhSach = v.findViewById(R.id.lv_danhsach_file);
    }

    private void listener(View v) {
        showList();
    }
    public void showList(){
        list = new ArrayList<>();
        String folderPath1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                List<File> mp3Files = getMp3FilesFromFolder(folderPath1);
                if(mp3Files.size()>0){
                    tvNoFile.setVisibility(View.GONE);
                    imgNoFile.setVisibility(View.GONE);
                }else {
                    tvNoFile.setVisibility(View.VISIBLE);
                    imgNoFile.setVisibility(View.VISIBLE);
                }
                for (File mp3File : mp3Files) {
                    list.add(new MP3model(mp3File.getName(), mp3File.getAbsolutePath(),mp3File.getParent()));
                    adapterMP3 = new AdapterMP3(getContext(), list);
                    lvDanhSach.setAdapter(adapterMP3);
                    adapterMP3.notifyDataSetChanged();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {

        }
        lvDanhSach.setOnItemClickListener((parent, view, position, id) -> {
//            openMp3File(list.get(position).getTitle(),list.get(position).getParent());
        });
    }
    private void openMp3File(String fileName, String folderPath) {
        File file = new File(folderPath, fileName);
        if (file.exists()) {
            Uri audioUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);

            Intent audioIntent = new Intent(Intent.ACTION_VIEW);
            audioIntent.setDataAndType(audioUri, "audio/*");
            audioIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                // Mở trình nghe nhạc để phát file MP3
                startActivity(audioIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}