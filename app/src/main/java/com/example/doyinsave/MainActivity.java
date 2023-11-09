package com.example.doyinsave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

import com.example.doyinsave.api.Client;
import com.example.doyinsave.api.TiktokService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {
    TextInputEditText edtsetLink;
    TiktokService service = Client.getInstance().getApi();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        setStatusBarGradiant(this);
        edtsetLink = findViewById(R.id.edt_setLink);
//        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);

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
                    String url = edtsetLink.getText().toString();
                    if (!url.isEmpty() && url.contains("tiktok")) {
//                        callAPI(url);
                        download(url);
                    } else {
                        Toast.makeText(MainActivity.this, "Enter a Valid URL!!", Toast.LENGTH_SHORT).show();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            }
        });
    }

    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.color.white);
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

    @SuppressLint("CheckResult")
    public void callAPI(String baseUrl) {
        //Reset cookie
        Client.getInstance().cookieInterceptor.cookie = "";
        //Get cookie + video url
        Observable<ResponseBody> response = service.getURL(baseUrl);
        response.flatMap(responseBody1 -> {
                    String responseStr = responseBody1.string();
                    String cookies = Client.getInstance().cookieInterceptor.getCookie();
                    String url = findString(responseStr);
                    Log.e("Cookies ", cookies);
                    Log.e("Url", url);
                    String decode = url.replaceAll("\\\\u002F", "/");
                    Log.e("Url decode", decode);
                    return service.getVideo(decode, cookies);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(responseBody2 -> {
                    Log.e("Response", "Success");
                    Log.e("Size", Client.getInstance().cookieInterceptor.getSize());
                    Log.e("Type", String.valueOf(responseBody2.contentType()));
                    //Save video
                    saveVideo(responseBody2, Integer.parseInt(Client.getInstance().cookieInterceptor.getSize()));
                }, Throwable::printStackTrace);
    }

    public String findString(String str) {
        String a = "downloadAddr";
        StringBuilder url = new StringBuilder();
        int b;
        if (!str.contains(a)) return null;
        b = str.indexOf(a) + 15;
        do {
            url.append(str.charAt(b));
            b++;
        } while (str.charAt(b) != '"');
        return url.toString();
    }

    public void saveVideo(ResponseBody body, int size) {
        // Tạo thư mục để lưu video
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyAppVideos");

        // Kiểm tra xem thư mục đã tồn tại chưa, nếu chưa thì tạo mới
        if (!directory.exists()) {
            directory.mkdirs();
        }


        // Tạo tên file dựa trên thời gian và ngày
        String fileName = "video_" + System.currentTimeMillis() + ".mp4";

        // Tạo đường dẫn đầy đủ đến file
        File file = new File(directory, fileName);

        try {
            // Mở FileOutputStream để ghi dữ liệu vào file
            FileOutputStream fos = new FileOutputStream(file);

            // Đọc dữ liệu từ InputStream và ghi vào file
            InputStream inputStream = body.byteStream();
            int read;
            byte[] buffer = new byte[size];
            while ((read = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }
            Log.e("Path", file.getAbsolutePath());
            // Đóng các luồng sau khi hoàn thành
            fos.close();
            inputStream.close();

            // Log thông báo thành công
            Log.e("Save", "Successful");


        } catch (IOException e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra
            e.printStackTrace();
        }
    }

    private void download(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://tiktok-video-no-watermark2.p.rapidapi.com/?url=" + url)
                        .get()
                        .addHeader("X-RapidAPI-Key", "73135137fcmsh589dcae25a44bf0p198052jsn12c80012d291")
                        .addHeader("X-RapidAPI-Host", "tiktok-video-no-watermark2.p.rapidapi.com")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        // Xem nội dung của Response
                        System.out.println("Response Body: " + responseBody);
                        processJsonResponse(responseBody);
                    } else {
                        // Xử lý khi tải không thành công
                        System.out.println("Error: " + response.code() + " - " + response.message());
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void processJsonResponse(String jsonResponse) {
        JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);

        // Kiểm tra xem có khối "data" không
        if (jsonObject.has("data")) {
            JsonObject dataObject = jsonObject.getAsJsonObject("data");

            if (dataObject.has("size") && dataObject.has("play")) {
                String Size = dataObject.get("size").getAsString();
                String Play = dataObject.get("play").getAsString();

                // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                System.out.println(" Size: " + Size);
                System.out.println(" Play URL: " + Play);
            }

            if (dataObject.has("wm_size") && dataObject.has("wmplay")) {
                String wmSize = dataObject.get("wm_size").getAsString();
                String wmPlay = dataObject.get("wmplay").getAsString();

                // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                System.out.println(" WMSize: " + wmSize);
                System.out.println(" WMPlay URL: " + wmPlay);
            }

            if (dataObject.has("hd_size") && dataObject.has("hdplay")) {
                String hdSize = dataObject.get("hd_size").getAsString();
                String hdPlay = dataObject.get("hdplay").getAsString();

                // Hiển thị hoặc sử dụng dữ liệu theo ý muốn
                System.out.println("HD Size: " + hdSize);
                System.out.println("HD Play URL: " + hdPlay);
            } else {
                System.out.println("Không có thông tin về HD Size hoặc HD Play trong response.");
            }
        } else {
            System.out.println("Không có khối dữ liệu (data) trong response.");
        }
    }
}
