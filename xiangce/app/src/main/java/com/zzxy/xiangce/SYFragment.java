package com.zzxy.xiangce;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zzxy.xiangce.utils.HttpUtils;

import java.util.List;


public class SYFragment extends Fragment implements View.OnClickListener{

    private static String TAG = SYFragment.class.getName();
    private View rootView;
    private RecyclerView cv;
    private MyRecyclerViewAdapter adapter;

    private LinearLayout sy_ll_btn;
    private Button btn_qx,btn_sc;

    public SYFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_sy, container, false);
            initView();
        }
        return rootView;
    }

    private void initView(){
        this.cv = rootView.findViewById(R.id.sy_rec);
        this.adapter = new MyRecyclerViewAdapter(this.getActivity());
        this.cv.setAdapter(this.adapter);

        this.cv.setLayoutManager(new LinearLayoutManager(this.getContext()));

        //图片上传界面组件
        this.sy_ll_btn = rootView.findViewById(R.id.sy_ll_btn);
        this.btn_qx = rootView.findViewById(R.id.sy_qx);
        this.btn_sc = rootView.findViewById(R.id.sy_sc);
        this.btn_sc.setOnClickListener(this);
        this.btn_qx.setOnClickListener(this);

        this.adapter.setMessage(new TMessage() {
            @Override
            public void sendMessage(int type, Object data) {
                if(type == 1){ //打开按钮
                    SYFragment.this.sy_ll_btn.setVisibility(View.VISIBLE);
                }else if(type == 2){ //关闭按钮
                    SYFragment.this.sy_ll_btn.setVisibility(View.GONE);
                }
            }
        });
    }

    //上传和取消按钮点击事件
    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.sy_qx){ //取消功能
            this.adapter.quitSelectImg();
            Log.d(TAG, "onClick: 取消按钮");
        }else if(v.getId() == R.id.sy_sc){ //上传功能

            Toast.makeText(this.getContext(),"上传中...",Toast.LENGTH_SHORT).show();
            //上传图片
            List<String> imgs = this.adapter.getAllSelectedImg();
            Log.d(TAG, "onClick: 上传按钮： 数据："+imgs.size());
            for(String img:imgs){
                HttpUtils.uploadImage(img, this.getContext());
            }
        }
    }
}
