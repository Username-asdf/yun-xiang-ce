package com.zzxy.xiangce.utils;

import android.content.Context;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.gson.Gson;
import com.zzxy.xiangce.LoginFragment;
import com.zzxy.xiangce.TMessage;
import com.zzxy.xiangce.pojo.Users;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class HttpUtils {
    private static String TAG = HttpUtils.class.getName();
    public static List<Cookie> cookies = new ArrayList<>();
    private static LazyHeaders header = null;
    private static OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {

                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    cookies.addAll(list);

                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return cookies;
                }
            }).build();

    public static OkHttpClient getClient() {
        return client;
    }


    /**
     * 获取cookie信息
     * @return
     */
    public static Headers getHeader(){
        if(header == null){
            StringBuilder stringBuilder = new StringBuilder();
            for (Cookie c:cookies) {
                stringBuilder.append(c.name())
                        .append("=")
                        .append(c.value())
                        .append(";");
            }
//            GET /gsi?imgName=9136d6fa-e0b2-4bc9-9f94-91ae33428416.jpg HTTP/1.1
//            Host: localhost
//            User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:98.0) Gecko/20100101 Firefox/98.0
//            Accept: image/avif,image/webp,*/*
//Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
//Accept-Encoding: gzip, deflate
//Connection: keep-alive
//Referer: http://localhost/showImage
//Cookie: JSESSIONID=0AEF18D39BB059A3747D511251C6756D; rememberMe=sXX9vGVZgitWIpmWIf/TQgSZhkUO+0BcNyX/rxlLd/lY5JAjifqK6DjX8bcadwURJOp4a9Bxf+enrHcze4BWsu8LD5iZSAGZp/LDHtV9ni6aKdmD+DcDpPc+H95WYzyoXXluCF/0bKkL/vzCDeL1vKlgey5Mfodk0EKotejoLL0gwsfJOSLwxEkwDe1+71kD1ahJivf+0DpZkZ1w9eNtJgGvLbikfo9sJfARHPvY4oUn3vntbyxZWX++TqOmoDhLXUZN4K1IQuUZyzFD3Xd88Jzh/0xOLYQTDh3ZjaT0COgP2EtCuAjc6qyATMLgQc1FFBmwbXQZjgJcAjwyvB2HzXxgMDuUWD7T5AmC01DwwGjBxXDwLchyD3nzWhV/mSiH9Qz0E4fXUOLx+j/h/Vz4iAFRNBhjBqTMPktGMMqC5h/Jwo9gqnHIePDFIqFIdWjROAIIIbPv2iaKOJoGfhLsx9R/lUG3x8Fu+OPkU1w2mrsEkjhC2BEWI52DWjcVbkSxFFPZleKJP8gaSSjrgpAHA3YQIyvZWIIun9T8c3c5FrrTYuCE6ff3iBi54mPtWfhVCNhKz/6RVrAS6Apt04eQLw9jPfs1L2iMqznAsEelWfx4pWneMgfGpQCWGIPIi3YQXfz6vwZR9vvXMBvmuwIe6LcFTyzWO54oHZXNo32VcyhdwnVUwjou2rYn4nXSaEBY3vYz92iL5mlDWMMYdZW2XTsLpNwJOJXCt/GVydWw0jf9iMUnlZXXSflYZ9ZY4FEmb72IaMlBhc0vSicW4KVf4Q==
//Sec-Fetch-Dest: image
//Sec-Fetch-Mode: no-cors
//Sec-Fetch-Site: same-origin
            header =  new LazyHeaders.Builder()
                    .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:98.0) Gecko/20100101 Firefox/98.0")
                    .addHeader("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .addHeader("Accept-Encoding","image/avif,image/webp,*/*")
                    .addHeader("Connection","keep-alive")
                    .addHeader("Cookie",stringBuilder.toString())
                    .build();
        }
        return header;
    }


    /**
     * 发起一个同步请求
     * @param url
     * @return 收到的数据
     */
    public static String get(String url){
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        Response execute = null;
        try {
            execute = call.execute();
            return execute.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发起一个异步get请求
     * @param url
     * @param callback
     */
    public static void get(String url, Callback callback){
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = client.newCall(request);
        if(callback != null){
            call.enqueue(callback);
        }else{
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                }
            });
        }
    }

    /**
     * 发起一个同步post请求
     * @param url
     * @param form
     * @return
     */
    public static String post(String url, FormBody form){
        if(form == null) {
            form = new FormBody.Builder().build();
        }
        Request request = new Request.Builder()
                .url(url)
                .post(form)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发起一个异步post请求
     * @param url
     * @param form
     * @param callback
     */
    public static void post(String url, FormBody form, Callback callback){
        if(form == null) {
            form = new FormBody.Builder().build();
        }
        Request request = new Request.Builder()
                .url(url)
                .post(form)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    //上传图片
    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    public static void uploadImage(String path,final Context context){
        String imgName = path.substring(path.lastIndexOf("/")+1);
        Log.d(TAG, "uploadImage: "+imgName);
        RequestBody fileBody = RequestBody.create(MEDIA_TYPE_JPG, new File(path));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imgName, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(Config.uploadImageUrl)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                if(Looper.myLooper() == null){
                    Looper.prepare();
                }
                Toast.makeText(context,"上传失败，请稍后再试！",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(Looper.myLooper() == null){
                    Looper.prepare();
                }
                Toast.makeText(context,"上传成功！！！",Toast.LENGTH_SHORT).show();

            }});
    }

}
