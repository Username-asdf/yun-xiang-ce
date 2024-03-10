package com.zzxy.xiangce;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.zzxy.xiangce.pojo.LoginResult;
import com.zzxy.xiangce.pojo.Users;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.HttpUtils;
import com.zzxy.xiangce.utils.UserUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ChangeAndZhuceFragment extends Fragment implements View.OnClickListener {
    public static int ZC=1, CP=2;
    private static String TAG = ChangeAndZhuceFragment.class.getName();
    private View rootView;
    private EditText username, password;
    private TextView title;
    private Button submit;
    private TMessage message;
    private Activity context;
    private Users user;
    private int type;

    public ChangeAndZhuceFragment(int type, Activity context, Users user) {
        this.type = type;
        this.context = context;
        this.user = user;
    }
    public ChangeAndZhuceFragment(){}

    public void setMessage(TMessage message){
        this.message = message;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        if(rootView == null){
            this.rootView = inflater.inflate(R.layout.fragment_cpandzc,container,false);
        }
        init(); //公共初始化组件
        if(type == ZC){
            initZC(); //初始化注册组件
        }else if(type == CP){
            initCP(); //初始化修改密码组件
        }
        return rootView;
    }

    private void initCP() {
        this.title.setText("修改密码");
        this.username.setText(user.getUsername());
        this.username.setEnabled(false);
    }

    private void initZC() {
        this.title.setText("注册");
    }

    private void init() {
        this.title = rootView.findViewById(R.id.tv_cz_title);
        this.username = rootView.findViewById(R.id.et_cz_username);
        this.password = rootView.findViewById(R.id.et_cz_password);
        this.submit = rootView.findViewById(R.id.btn_cz_submit);
        this.submit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK){
                    Log.d(TAG, "onKey: 按了返回按钮");
                    //跳转到原页面
                    if(message!=null){
                        message.sendMessage(TMessage.CHANGE_UI, type);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(this.type == ZC){
            final String usernaemStr = username.getText().toString();
            final String passwordStr = password.getText().toString();

            FormBody form = new FormBody.Builder()
                    .add("username", usernaemStr)
                    .add("password", passwordStr)
                    .add("repassword", passwordStr)
                    .build();
            HttpUtils.post(Config.registerUrl, form, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "onFailure: "+e.getMessage());
                    if(Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    Toast.makeText(context,"注册失败，请稍后再试！",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    if(UserUtils.optIsSuccess(response)){
                        Toast.makeText(context,"注册成功",Toast.LENGTH_SHORT).show();
                        //自动登录
                        UserUtils.reLogin(usernaemStr, passwordStr,MainActivity.login_user_key, context);
                        context.runOnUiThread(new Thread(){
                            @Override
                            public void run() {
                                message.sendMessage(TMessage.CHANGE_AND_DATA, new Users(usernaemStr, passwordStr));
                            }
                        });
                    }else{
                        Toast.makeText(context,"注册失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            final String passwordStr = password.getText().toString();

            FormBody form = new FormBody.Builder()
                    .add("oldPassword", user.getPassword())
                    .add("password", passwordStr)
                    .add("repassword", passwordStr)
                    .build();
            HttpUtils.post(Config.updatePassword, form, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "onFailure: "+e.getMessage());
                    if(Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    Toast.makeText(context,"修改失败，请稍后再试！",Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    if(UserUtils.optIsSuccess(response)){
                        Toast.makeText(context,"修改成功",Toast.LENGTH_SHORT).show();
                        //重新登录
                        context.runOnUiThread(new Thread(){
                            @Override
                            public void run() {
                                message.sendMessage(TMessage.CHANGE_UI, ChangeAndZhuceFragment.ZC);
                            }
                        });
                    }else{
                        Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
