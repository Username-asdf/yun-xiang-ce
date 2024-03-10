package com.zzxy.xiangce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zzxy.xiangce.pojo.Users;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.HttpUtils;
import com.zzxy.xiangce.utils.PermissionUtils;
import com.zzxy.xiangce.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = MainActivity.class.getName();
    private ViewPager2 viewpager;
    private LinearLayout syLayout, wdLayout;
    private ImageView syiv, wdiv, curriv;
    public static String login_user_key = "user";
    private LoginFragment loginFragment;
    private SYFragment syFragment;
    private WDFragment wdFragment;
    private ChangeAndZhuceFragment czFragment;

    public static Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //申请读写储存权限
        PermissionUtils.requestReadAndWriteStorge(this);

        initSession();
        initLogin();
        initPager();
        initTab();
    }

    private void initSession() {
        HttpUtils.get(Config.loginUrl,null);
    }

    private void initLogin() {
        String userString = UserUtils.getValue(this, login_user_key);
        if(!(userString.equals("")|| userString==null)){
            user = new Gson().fromJson(userString, Users.class);
            //重新的登录
            UserUtils.reLogin(user.getUsername(), user.getPassword(), login_user_key,this);
        }
    }

    private void initTab() {
        syLayout = findViewById(R.id.tab_sy);
        syLayout.setOnClickListener(this);
        wdLayout = findViewById(R.id.tab_wd);
        wdLayout.setOnClickListener(this);
        syiv = findViewById(R.id.tab_sy_iv);
        wdiv = findViewById(R.id.tab_wd_iv);
        curriv = syiv;
        curriv.setSelected(true);
    }

    private void initPager() {
        final List<Fragment> fragments = new ArrayList<>();
        final MyFragmentAdapter fragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
        viewpager = findViewById(R.id.viewpager2);
        this.syFragment = new SYFragment();
        this.wdFragment = new WDFragment(user, this);
        this.loginFragment = new LoginFragment(this);
        czFragment = new ChangeAndZhuceFragment(ChangeAndZhuceFragment.ZC, this, null);
        final TMessage cz_message = new TMessage() {
            @Override
            public void sendMessage(int type, Object data) {
                if (type == TMessage.CHANGE_UI) {
                    if (((Integer) data).intValue() == ChangeAndZhuceFragment.CP) {
                        //跳转到我的页面
                        fragments.set(1, wdFragment);
                        fragmentAdapter.setFragments(fragments);
                    } else {
                        //跳转到登录界面
                        fragments.set(1, loginFragment);
                        fragmentAdapter.setFragments(fragments);
                    }
                } else if (type == TMessage.CHANGE_AND_DATA) { //登录成功后跳转到我的页面
                    Users user = (Users) data;
                    Log.d(TAG, "sendMessage: " + user.getUsername());
                    fragments.set(1, wdFragment);
                    wdFragment.setUser(user);
                    fragmentAdapter.setFragments(fragments);
                }
            }
        };
        czFragment.setMessage(cz_message);
        this.wdFragment.setMessage(new TMessage() {
            @Override
            public void sendMessage(int type, Object data) {
                if(type == TMessage.CHANGE_UI){
                    //退出登录跳转到登录界面
                    Log.d(TAG, "sendMessage: 跳转到登录界面");
                    fragments.set(1, loginFragment);
                    fragmentAdapter.setFragments(fragments);
                }else if(type == TMessage.CHANGE_AND_DATA){
                    Users user = (Users) data;
                    czFragment = new ChangeAndZhuceFragment(ChangeAndZhuceFragment.CP,MainActivity.this, user);
                    czFragment.setMessage(cz_message);
                    fragments.set(1, czFragment);
                    fragmentAdapter.setFragments(fragments);
                }
            }
        });
        this.loginFragment.setTMessage(new TMessage() {
            @Override
            public void sendMessage(int type,Object data) {
                if(type == TMessage.CHANGE_AND_DATA){
                    Users user = (Users)data;
                    //登录成功切换到我的界面
                    fragments.set(1, wdFragment);
                    wdFragment.setUser(user);
                    fragmentAdapter.setFragments(fragments);
                }else if(type == TMessage.CHANGE_UI){

                    fragments.set(1, czFragment);
                    fragmentAdapter.setFragments(fragments);
                }

            }
        });
        fragments.add(this.syFragment);
        if(user == null){
            fragments.add(loginFragment);
        }else{
            fragments.add(this.wdFragment);
        }


        viewpager.setAdapter(fragmentAdapter);
        viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                curriv.setSelected(false);
                if(position == 0){
                    curriv = syiv;
                }else if(position == 1){
                    curriv = wdiv;
                }
                curriv.setSelected(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        curriv.setSelected(false);
        switch (v.getId()){
            case R.id.tab_sy:
                viewpager.setCurrentItem(0);
                curriv = syiv;
                break;
            case R.id.tab_wd:
                curriv = wdiv;
                viewpager.setCurrentItem(1);
                break;
        }
        curriv.setSelected(true);
    }

    private void init_fragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();



        //transaction.replace(R.id.fl_main, blankFragment);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            //获取权限成功 重新初始化page
            initSession();
            initLogin();
            initPager();
            initTab();
        }else{
            Toast.makeText(this, "获取权限失败", Toast.LENGTH_LONG);
        }
    }
}
