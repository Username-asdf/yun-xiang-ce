package com.zzxy.xiangce.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zzxy.xiangce.TMessage;
import com.zzxy.xiangce.pojo.LoginResult;
import com.zzxy.xiangce.pojo.Users;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class UserUtils {

    private static String TAG = UserUtils.class.getName();
    private static String data_file_name = "data";
    /**
     * 判断用户是否登录成功
     * @param response
     * @return
     */
    public static boolean isLogin(Response response){
        try {
            LoginResult loginResult = new Gson().fromJson(response.body().string(), LoginResult.class);
            if(loginResult.getCode().intValue() == 200){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isLogin(String str){
        LoginResult loginResult = new Gson().fromJson(str, LoginResult.class);
        if(loginResult.getCode().intValue() == 200){
            return true;
        }
        return false;
    }

    /**
     * 将数据保存在data文件中
     * @param context
     * @param key
     * @param value
     */
    public static void saveValue(Context context, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(data_file_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    /**
     * 获取data文件的数据
     * @param context
     * @param key
     * @return
     */
    public static String getValue(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(data_file_name, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    /**
     * 删除data文件下指定的数据
     * @param context
     * @param key
     */
    public static void deleteValue(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(data_file_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key);
        edit.commit();
    }

    /**
     * 清除data文件下所有数据
     * @param context
     */
    public static void clearValue(Context context){
        SharedPreferences sp = context.getSharedPreferences(data_file_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * 判断返回码是否是200
     * @param resp
     * @return
     */
    public static boolean optIsSuccess(Response resp){
        try {
            String body = resp.body().string();
            Log.d(TAG, "optIsSuccess: "+body);
           return new Gson().fromJson(body, LoginResult.class).getCode()== 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 重新登录
     * @param username
     * @param password
     * @param login_user_key
     * @param context
     */
    public static void reLogin(final String username, final String password, final String login_user_key, final Context context){
        FormBody form = new FormBody.Builder()
                .add("username",username)
                .add("password",password)
                .build();
        HttpUtils.post(Config.loginUrl, form, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(UserUtils.isLogin(response)){
                    UserUtils.saveValue(context,login_user_key, new Gson().toJson(new Users(username, password)));
                }else{
                    UserUtils.deleteValue(context, login_user_key);
                }
            }
        });
    }

}
