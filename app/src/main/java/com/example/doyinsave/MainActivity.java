package com.example.doyinsave;

import static android.content.ContentValues.TAG;
import static com.example.doyinsave.utils.FileHelper.getMp3FilesFromFolder;
import static com.example.doyinsave.utils.FileHelper.getMp4FilesFromFolder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;

import com.example.doyinsave.adapter.AdapterMP3;
import com.example.doyinsave.adapter.AdapterMP4_1;
import com.example.doyinsave.model.MP3model;
import com.example.doyinsave.model.MP4model;
import com.example.doyinsave.utils.NetworkUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextInputEditText edtsetLink;
    private AlertDialog dialog;
    private Disposable disposable;
    private RewardedAd rewardedAd;
    boolean isLoading;
    private ListView lv_JustDownload;
    List<MP3model> itemMp3;
    List<MP4model> itemMp4;
    private long downloadID;
    private Handler handler;
    private static final int DELAY_MILLIS = 1000;
    boolean isceck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        setStatusBarGradiant(this);
        edtsetLink = findViewById(R.id.edt_setLink);
        lv_JustDownload = findViewById(R.id.lv_just_download);
        AlertDialog(this);
        listener();
        loadRewardedAd();
        getReceivedData();
        if (isStoragePermissionGranted()) {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name));
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
    }

    private void listener() {
        itemMp3 = new ArrayList<>();
        itemMp4 = new ArrayList<>();
        handler = new Handler();
        findViewById(R.id.btn_paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";

                if (!(clipboard.hasPrimaryClip())) {
                } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
                } else {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    pasteData = item.getText().toString();
                    edtsetLink.setText(pasteData);
                }
            }
        });

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
//                    if (NetworkUtils.isNetworkConnected(MainActivity.this)) {
//
//                    } else {
//                        Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
//                    }
                    if (!NetworkUtils.isWifiConnected(MainActivity.this)&&!NetworkUtils.isMobileDataConnected(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    } else {
                        String url = edtsetLink.getText().toString();
                        if (!url.isEmpty() && url.contains("tiktok")) {
                            Observable<String> observable = getResponseBody(url);
                            Observer<String> observer = getObserverBody();
                            observable.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(observer);
                            dialog.show();
                        } else {
                            Toast.makeText(MainActivity.this, "Enter a Valid URL!!", Toast.LENGTH_SHORT).show();
                        }
                    }


                }
            }
        });

        findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HelpsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.color.black);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }


    private void processJsonResponseYi005(String jsonResponse) {
        JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);
        // Kiểm tra xem có khối "data" không
        if (jsonObject.has("data")) {
            JsonObject dataObject = jsonObject.getAsJsonObject("data");
            JsonObject dataObjectMS = jsonObject.getAsJsonObject("data").getAsJsonObject("music_info");
            showBottomSheetDialog(dataObject, dataObjectMS);

        } else {
            System.out.println("Không có khối dữ liệu (data) trong response.");
        }
    }

    private void processJsonResponseMaatootz(String jsonResponse) {
        JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);
        if (jsonResponse != null) {
            // Lấy các giá trị từ JSON và hiển thị chúng trên giao diện người dùng
            String videoUrl = jsonObject.getAsJsonArray("video").get(0).getAsString();
            String musicUrl = jsonObject.getAsJsonArray("music").get(0).getAsString();
            String videoid = jsonObject.getAsJsonArray("videoid").get(0).getAsString();
            showBottomDialogMaatootz(videoUrl, musicUrl, videoid);
        }
    }

    private void showBottomDialogMaatootz(String Play, String PlayMP3, String id) {
        Dialog bottomSheetDialog = new Dialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_download);
        LinearLayout btnMp4 = bottomSheetDialog.findViewById(R.id.btn_downloadmp4);
        LinearLayout btnMp3 = bottomSheetDialog.findViewById(R.id.btn_downloadmp3);
        TextView tvNameMp3 = bottomSheetDialog.findViewById(R.id.tv_namefilemp3);
        TextView tvNameMp4 = bottomSheetDialog.findViewById(R.id.tv_namefilemp4);
        ImageView btnClose = bottomSheetDialog.findViewById(R.id.img_close);
        View decorView = bottomSheetDialog.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //
        Window window = bottomSheetDialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        window.setAttributes(windowAttributes);
        windowAttributes.gravity = Gravity.BOTTOM;
        btnMp4.setOnClickListener(view -> {
            String fileNameMP4 = "ct" + id + System.currentTimeMillis() + ".mp4";
            tvNameMp4.setText(fileNameMP4);
            startDownload(Play, fileNameMP4);
            bottomSheetDialog.dismiss();
        });

        btnMp3.setOnClickListener(view -> {
            String fileNameMP3 = "ct" + id + System.currentTimeMillis() + ".mp3";
            tvNameMp3.setText(fileNameMP3 + ".mp3");
            startDownload(PlayMP3, fileNameMP3);
            bottomSheetDialog.dismiss();
        });
        btnClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void showBottomSheetDialog(JsonObject dataObject, JsonObject dataObjectMS) {
        Dialog bottomSheetDialog = new Dialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_download);
        LinearLayout btnMp4 = bottomSheetDialog.findViewById(R.id.btn_downloadmp4);
        LinearLayout btnMp3 = bottomSheetDialog.findViewById(R.id.btn_downloadmp3);
        TextView tvNameMp3 = bottomSheetDialog.findViewById(R.id.tv_namefilemp3);
        TextView tvNameMp4 = bottomSheetDialog.findViewById(R.id.tv_namefilemp4);
        ImageView btnClose = bottomSheetDialog.findViewById(R.id.img_close);
        View decorView = bottomSheetDialog.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //
        Window window = bottomSheetDialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        window.setAttributes(windowAttributes);
        windowAttributes.gravity = Gravity.BOTTOM;
        btnMp4.setOnClickListener(view -> {
            if (dataObject.has("hd_size") && dataObject.has("hdplay")) {
                String SizeMP4 = dataObject.get("hd_size").getAsString();
                String PlayMP4 = dataObject.get("hdplay").getAsString();
                String id = dataObject.get("id").getAsString();
                // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                System.out.println("HD Size: " + SizeMP4);
                System.out.println("HD Play URL: " + PlayMP4);
                String fileNameMP4 = "videoFHD_" + id + System.currentTimeMillis() + ".mp4";
                tvNameMp4.setText(fileNameMP4);

                startDownload(PlayMP4, fileNameMP4);
            } else {
                if (dataObject.has("size") && dataObject.has("play")) {
                    String SizeMP4 = dataObject.get("size").getAsString();
                    String Play = dataObject.get("play").getAsString();
                    String id = dataObject.get("id").getAsString();
                    // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                    System.out.println(" Size: " + SizeMP4);
                    System.out.println(" Play URL: " + Play);
                    String fileNameMP4 = "ct" + id + System.currentTimeMillis() + ".mp4";
                    tvNameMp4.setText(fileNameMP4);
                    startDownload(Play, fileNameMP4);
                }
            }
            bottomSheetDialog.dismiss();
        });

        btnMp3.setOnClickListener(view -> {
            if (dataObjectMS.has("play")) {
                String PlayMP3 = dataObjectMS.get("play").getAsString();
                String id = dataObjectMS.get("id").getAsString();
                // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                System.out.println(" Play URL: " + PlayMP3);
                String fileNameMP3 = "ct" + id + System.currentTimeMillis() + ".mp3";
                tvNameMp3.setText(fileNameMP3 + ".mp3");
                startDownload(PlayMP3, fileNameMP3);
            }
            bottomSheetDialog.dismiss();
        });
        btnClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        // Hiển thị BottomSheetDialog
        bottomSheetDialog.show();
    }

    private void startDownload(String url, String namefile) {
        itemMp3.clear();
        itemMp4.clear();
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(getString(R.string.download));
        request.setDescription(namefile);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/" + getString(R.string.app_name), namefile);
        request.setMimeType("application/vnd.android.package-archive");
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadID = downloadManager.enqueue(request);
            isceck = true;
            startDownloadStatusBroadcastLoop(downloadID, namefile);
        }
    }

    private void handleDownloadStatus(int status, String namefile1) {
        if (status == DownloadManager.STATUS_SUCCESSFUL) {

            if (namefile1.contains(".")) {
                String result = namefile1.substring(namefile1.lastIndexOf(".") + 1);
                System.out.println(result); // In ra phần mở rộng của tệp

                String folderPath1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + getString(R.string.app_name);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        if (result.equals("mp3")) {
                            itemMp3 = new ArrayList<>();
                            List<File> mp3Files = getMp3FilesFromFolder(folderPath1);
                            for (File mp3File : mp3Files) {
                                if (mp3File.getName().equals(namefile1)) {
                                    itemMp3.add(new MP3model(mp3File.getName(), mp3File.getAbsolutePath(), mp3File.getParent()));
                                    AdapterMP3 adapterMP3 = new AdapterMP3(MainActivity.this, itemMp3,R.layout.item_mp4_1);
                                    lv_JustDownload.setAdapter(adapterMP3);
                                }


                            }
                        } else if (result.equals("mp4")) {
                            itemMp4 = new ArrayList<>();
                            List<File> mp4Files = getMp4FilesFromFolder(folderPath1);
                            for (File mp4File : mp4Files) {
                                if (mp4File.getName().equals(namefile1)) {
                                    itemMp4.add(new MP4model(mp4File.getName(), mp4File.getAbsolutePath(), mp4File.getParent()));
                                    AdapterMP4_1 adapterMP3 = new AdapterMP4_1(MainActivity.this, itemMp4);
                                    lv_JustDownload.setAdapter(adapterMP3);
                                }
                            }

                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                }

            }
            stopDownloadStatusBroadcastLoop();
        } else {
        }
    }


    private void AlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_progress_dialog, null);
        // Set the custom view to the AlertDialog
        builder.setView(view);
        // Create the AlertDialog
        dialog = builder.create();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        // Set the AlertDialog not cancelable with back button or touch outside
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

    private Observer<String> getObserverBody() {
        return new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.e("onSubscribe", "onSubscribe");
                disposable = d;
            }

            @Override
            public void onNext(@NonNull String s) {
                dialog.dismiss();
                Log.e("onNext", "onNext" + s);
//                processJsonResponseYi005(s);
                processJsonResponseMaatootz(s);
                if (disposable != null) {
                    disposable.dispose();
                }

            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e("onError", "onError");
            }

            @Override
            public void onComplete() {
                Log.e("onComplete", "onComplete");
            }
        };
    }

    private Observable<String> getResponseBody(String url) {

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
//                        .url(getString(R.string.url_api_yi005) + url)
//                        .url(getString(R.string.url_api_yi005_1) + url)
                        .url(getString(R.string.url_api_maatootz) + url)
                        //
                        .get()
                        .addHeader("X-RapidAPI-Key", getString(R.string.api_key))
                        //
//                        .addHeader("X-RapidAPI-Host", getString(R.string.api_host_yi005))
                        //
//                        .addHeader("X-RapidAPI-Host", getString(R.string.api_host_yi005_1))
                        .addHeader("X-RapidAPI-Host", getString(R.string.api_host_maatootz))
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful() || response == null) {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new IOException());
                        }
                    }
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        if (!emitter.isDisposed()) {
                            emitter.onNext(responseBody);
                            System.out.println("Response Body: " + responseBody);
                        }

//                        processJsonResponse(responseBody);
                    } else {
                        // Xử lý khi tải không thành công
                        System.out.println("Error: " + response.code() + " - " + response.message());
                    }
                    if (!emitter.isDisposed()) {
                        emitter.onComplete();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    getString(R.string.AD_UNIT_ID),
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@androidx.annotation.NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
//                            Toast.makeText(MainActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@androidx.annotation.NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            MainActivity.this.isLoading = false;
//                            Toast.makeText(MainActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void showRewardedVideo() {
        if (rewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "onAdShowedFullScreenContent");
//                        Toast.makeText(MainActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@androidx.annotation.NonNull AdError adError) {
                        rewardedAd = null;
                        Log.d(TAG, "Error:" + adError);
//                        Toast.makeText(
//                                        MainActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
//                        Toast.makeText(MainActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
//                                .show();
                        MainActivity.this.loadRewardedAd();

                    }
                });
        Activity activityContext = MainActivity.this;
        rewardedAd.show(
                activityContext,
                rewardItem -> {
                    // Handle the reward.
                    Log.d("TAG", "The user earned the reward.");
                });
    }

    private void getReceivedData() {
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                edtsetLink.setText(sharedText);
            }
        }
    }

    private void startDownloadStatusBroadcastLoop(long downloadID, String namefile) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isceck) {
                    sendDownloadStatusBroadcast(downloadID, namefile);
                    handler.postDelayed(this, DELAY_MILLIS);
                }

            }
        }, DELAY_MILLIS);
    }

    private void stopDownloadStatusBroadcastLoop() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            isceck = false;
        }
    }

    private void sendDownloadStatusBroadcast(long downloadId, String namefile) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(statusIndex);

                    handleDownloadStatus(status, namefile);
                }
            }
        }
    }
}
