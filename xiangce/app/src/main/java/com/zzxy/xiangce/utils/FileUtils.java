package com.zzxy.xiangce.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.PathUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FileUtils {

    public static void copy(Activity activity,File source, boolean notice) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        String imageName = System.currentTimeMillis() + ".jpg";
        File f = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "copy: path:"+f.getAbsolutePath());

        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(f.getAbsoluteFile()+"/"+imageName);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    Log.d(TAG, "copy: fileoutput");
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(notice){ //通知相册
            noticeXiangce(activity, new File(f.getAbsolutePath()+"/"+imageName),imageName);
        }
    }

    public static void copy(Activity activity,byte[] source,String saveName ,boolean notice) {
        FileOutputStream fileOutputStream = null;
        String imageName = saveName;
        File f = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "copy: path:"+f.getAbsolutePath());

        try {
            fileOutputStream = new FileOutputStream(f.getAbsoluteFile()+"/"+imageName);
            fileOutputStream.write(source);
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(notice){ //通知相册
            noticeXiangce(activity, new File(f.getAbsolutePath()+"/"+imageName),imageName);
        }
    }

    public static void noticeXiangce(final Activity activity, final File file, final String imageName){
        // 下面的步骤必须有，不然在相册里找不到图片，若不需要让用户知道你保存了图片，可以不写下面的代码。
        // 把文件插入到系统图库
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                            file.getAbsolutePath(), imageName, null);
                    //ToastUtil.showToast( "保存成功，请您到 相册/图库 中查看");
                } catch (FileNotFoundException e) {
                    //ToastUtil.showToast( "保存失败");
                    e.printStackTrace();
                }
                // 最后通知图库更新
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(file.getPath()))));
            }
        });

    }

    public static void saveImgToLocal(final Activity context,final String url,final boolean notice) {
        HttpUtils.get(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: 下载图片失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d(TAG, "onResponse: 下载图片成功");
                response.header("");
                byte[] data = response.body().bytes();
                copy(context, data,url.substring(url.indexOf("=")+1), notice);
            }
        });
    }

    /*
     * 将图片 bitmap保存到图库
     */
    public static void saveBitmap(Activity activity, Bitmap bitmap) {
        //因为xml用的是背景，所以这里也是获得背景
//获取参数Bitmap方式一： Bitmap bitmap=((BitmapDrawable)(imageView.getBackground())).getBitmap();
//获取参数Bitmap方式二： Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        //t设置图片名称，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
        String imageName = System.currentTimeMillis() + ".jpg";

        //创建文件，安卓低版本的方式
        //  File file=new File(Environment.getExternalStorageDirectory() +"/test.png");

        //Android Q  10为每个应用程序提供了一个独立的在外部存储设备的存储沙箱，没有其他应用可以直接访问您应用的沙盒文件
        File f = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(f.getPath() + "/"+imageName);//创建文件
        //        file.getParentFile().mkdirs();
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
            Log.e("写入成功！位置目录", f.getPath() + "/"+imageName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //失败的提示，这里的Toast工具类，大家用自己项目中的即可，若不需要可以删除
            //ToastUtil.showToast(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
            //ToastUtil.showToast(e.getMessage());
        }

        // 下面的步骤必须有，不然在相册里找不到图片，若不需要让用户知道你保存了图片，可以不写下面的代码。
        // 把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(activity.getContentResolver(),
//                    file.getAbsolutePath(), imageName, null);
//            //ToastUtil.showToast( "保存成功，请您到 相册/图库 中查看");
//        } catch (FileNotFoundException e) {
//            //ToastUtil.showToast( "保存失败");
//            e.printStackTrace();
//        }
//        // 最后通知图库更新
//        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                Uri.fromFile(new File(file.getPath()))));

    }
}
