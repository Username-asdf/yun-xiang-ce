package com.zzxy.xiangce.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ImagesDataUtils {
    private static ArrayList<String> imgData;
    public static ArrayList<String> getImgData(Context context){
        if(imgData == null){
            imgData = new ArrayList<>();
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            // 获得图片
            Cursor cursor = context.getContentResolver().query(mImageUri, null,
                    null,
                    null,MediaStore.Images.ImageColumns.DATE_ADDED+" desc");
            String[] columnNames = cursor.getColumnNames();

            //获取图片名称
//            String imgName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片绝对路径
//            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            while(cursor.moveToNext()){
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                imgData.add(path);
            }
        }
        return imgData;
    }
}
