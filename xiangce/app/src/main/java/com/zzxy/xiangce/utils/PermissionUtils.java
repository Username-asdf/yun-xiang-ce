package com.zzxy.xiangce.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 获取权限类
 */
public class PermissionUtils {

    /**
     * 请求获取读写储存权限
     * @param context
     * @return
     */
    public static void requestReadAndWriteStorge(Activity context){
        if(!isHaveReadAndWriteStorge(context))
            ActivityCompat.requestPermissions(context, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
    }

    /**
     * 判断是否有读写储存的权限
     * @param context
     * @return
     */
    public static boolean isHaveReadAndWriteStorge(Context context){
        //判断当前版本是否大于6.0, 大于6.0默认获取权限
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                return true;
            }
            return false;
        }
        return true;
    }

}
