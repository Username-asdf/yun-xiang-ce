package com.zzxy.xiangce;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzxy.xiangce.pojo.Users;
import com.zzxy.xiangce.utils.UserUtils;


public class WDFragment extends Fragment implements View.OnClickListener{

    private static String TAG = WDFragment.class.getName();
    private View rootView;
    private Users user;
    private Button logout, cp;
    private Context context;
    private TMessage message;
    private TextView username;
    private LinearLayout ykj_ll;

    public WDFragment(Users user, Context context) {
        this.user = user;
        this.context = context;
    }
    public WDFragment(){}

    public void setUser(Users user){
        this.user = user;
        if(username != null){
            this.username.setText("欢迎光临，"+user.getUsername());
        }
    }

    public void setMessage(TMessage message){
        this.message = message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_wd, container, false);
            initView();
        }
        return rootView;
    }

    private void initView() {
        this.ykj_ll = rootView.findViewById(R.id.ykj_ll);
        this.ykj_ll.setOnClickListener(this);
        this.logout = rootView.findViewById(R.id.logout);
        this.logout.setOnClickListener(this);
        this.cp = rootView.findViewById(R.id.btn_wd_cp);
        this.cp.setOnClickListener(this);
        //替换用户名
        this.username = rootView.findViewById(R.id.tv_wd_username);
        this.username.setText("欢迎光临，"+(this.user==null?"":this.user.getUsername()));
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.logout: //退出登录
                UserUtils.deleteValue(context, MainActivity.login_user_key);
                //跳转到登录界面
                if(message!=null){
                    message.sendMessage(TMessage.CHANGE_UI,null);
                }
                break;
            case R.id.btn_wd_cp: //修改密码
                if(message!=null){
                    Log.d(TAG, "onClick: 修改密码");
                    message.sendMessage(TMessage.CHANGE_AND_DATA, user);
                    Log.d(TAG, "onClick: 修改密码完成");
                }
                break;
            case R.id.ykj_ll:
                Intent intent = new Intent(getContext(),YKjActivity.class);
                getContext().startActivity(intent);
                break;
        }
    }
}
