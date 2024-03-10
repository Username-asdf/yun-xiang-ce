package com.zzxy.xiangce;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.zzxy.xiangce.pojo.Users;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.HttpUtils;
import com.zzxy.xiangce.utils.UserUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;



/**
 * 登录页面
 */
public class LoginFragment extends Fragment implements View.OnClickListener{

    private static String TAG = LoginFragment.class.getName();
    private View fragment_view;
    private EditText et_username, et_password;
    private TextView tv_zc;
    private ImageView iv_zc;
    private Button btn_submit;
    private Activity context;
    private TMessage message;

    public LoginFragment() {
    }

    public void setTMessage(TMessage message){
        this.message = message;
    }

    public LoginFragment(Activity context){
        this.context = context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(fragment_view == null){
            fragment_view = inflater.inflate(R.layout.fragment_login,container,false);
        }
        init();
        return fragment_view;
    }

    private void init(){

        et_username = fragment_view.findViewById(R.id.et_username);
        et_password = fragment_view.findViewById(R.id.et_password);
        btn_submit = fragment_view.findViewById(R.id.btn_submit);
        tv_zc = fragment_view.findViewById(R.id.tv_zc);
        iv_zc = fragment_view.findViewById(R.id.iv_zc);

        //设置注册点击时间
        tv_zc.setOnClickListener(this);
        iv_zc.setOnClickListener(this);

        //设置登录点击事件
        btn_submit.setOnClickListener(this);

    }

    //登录方法
    //登录成功保存用户名和密码
    public void login(final String username, final String password, final String login_user_key, final Activity context){
        Log.d(TAG, "login: 3");
        FormBody form = new FormBody.Builder()
                .add("username",username)
                .add("password",password)
                .build();
        HttpUtils.post(Config.loginUrl, form, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
                if(Looper.myLooper() == null){
                    Looper.prepare();
                }
                Toast.makeText(context,"登录失败，请稍后再试！",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(Looper.myLooper() == null){
                    Looper.prepare();
                }
                if(UserUtils.isLogin(response)){

                    Log.d(TAG, "onResponse: qwer");
                    Toast.makeText(context,"登录成功",Toast.LENGTH_SHORT).show();
                    UserUtils.saveValue(context,login_user_key, new Gson().toJson(new Users(username, password)));


                    context.runOnUiThread(new Thread(){
                        @Override
                        public void run() {
                            Log.d(TAG, "run: 1");
                            LoginFragment.this.message.sendMessage(TMessage.CHANGE_AND_DATA, new Users(username, password));
                            Log.d(TAG, "run: 2");
                        }
                    });
                }else{
                    Toast.makeText(context,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: 1");
        switch(v.getId()){
            case R.id.tv_zc:
            case R.id.iv_zc: //切换到注册页面
                message.sendMessage(TMessage.CHANGE_UI, null);
                break;
            case R.id.btn_submit:
                Log.d(TAG, "onClick: 2");
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                login(username, password, MainActivity.login_user_key,context);
                break;

        }
    }
}
