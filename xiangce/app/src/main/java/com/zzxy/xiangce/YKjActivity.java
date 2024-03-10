package com.zzxy.xiangce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zzxy.xiangce.pojo.ImageInfo;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.FileUtils;
import com.zzxy.xiangce.utils.HttpUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class YKjActivity extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = YKjActivity.class.getName();
    public RecyclerView rv;
    private YkjAdapter ykjAdapter;
    private List<String> fidStack = new ArrayList<>();// 文件夹回退栈

    //下载，删除，取消功能按钮
    private Button btn_qx,btn_xz,btn_del;
    private LinearLayout ll;


    LinearLayoutManager manager = new LinearLayoutManager(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ykj);



        this.rv = findViewById(R.id.ykj_rv);
        this.ykjAdapter = new YkjAdapter(this,Config.getFolderFile);
        this.rv.setAdapter(this.ykjAdapter);
        this.rv.setLayoutManager(manager);

        this.btn_qx = findViewById(R.id.ykj_qx);
        this.btn_del = findViewById(R.id.ykj_del);
        this.btn_xz = findViewById(R.id.ykj_xz);
        this.ll = findViewById(R.id.ykj_ll_btn);

        this.btn_qx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消按钮
                YKjActivity.this.ykjAdapter.quitSelected();
                YKjActivity.this.ykjAdapter.notifyDataSetChanged();
                YKjActivity.this.ll.setVisibility(View.GONE);
            }
        });
        this.btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除按钮

                List<ImageInfo> imagesInfos = YKjActivity.this.ykjAdapter.getAllSelectedImage();
                for(ImageInfo info: imagesInfos){
                    Log.d(TAG, "onClick: 删除按钮 :"+Config.delImageUrl+"?id="+info.getId());
                    if(info.getIsimg() ==1){ //删除图片
                        HttpUtils.get(Config.delImageUrl+"?id="+info.getId(), null);
                    }else{ //删除文件夹
                        HttpUtils.get(Config.delImageFolder+"?fid="+info.getId(), null);
                    }
                    //将元素删除
                    YKjActivity.this.ykjAdapter.getData().remove(info);
                }

                YKjActivity.this.ykjAdapter.quitSelected();
                YKjActivity.this.ykjAdapter.notifyDataSetChanged();
                YKjActivity.this.ll.setVisibility(View.GONE);
            }
        });
        this.btn_xz.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //下载按钮
                List<ImageInfo> imagesInfos = YKjActivity.this.ykjAdapter.getAllSelectedImage();
                for(ImageInfo info:imagesInfos){
                    if(info.getIsimg() ==1){ //下载图片
                        FileUtils.saveImgToLocal(YKjActivity.this,
                                Config.downloadUrl+"?filename="+info.getRealname(), true);
                    }else{ //下载文件夹的图片
                        YKjActivity.this.downloadFolder(info.getId());
                    }
                }
                YKjActivity.this.ykjAdapter.quitSelected();
                YKjActivity.this.ykjAdapter.notifyDataSetChanged();
                YKjActivity.this.ll.setVisibility(View.GONE);
            }
        });

    }

    public void downloadFolder(long fid){
        FormBody formBody = new FormBody.Builder()
                .add("pageSize","9999")
                .add("pageNum", "1")
                .add("fid", "0")
                .build();
        HttpUtils.post(Config.getFolderFile+"?fid="+fid, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: 获取文件夹下所有文件失败");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String body = response.body().string();
                if(body == null) {
                    Log.d(TAG, "onResponse:获取数据失败");
                    return;
                }
                List<ImageInfo> data = (List<ImageInfo>) new Gson().fromJson(body, new TypeToken<List<ImageInfo>>(){}.getType());

                for(ImageInfo info: data){
                    if(info == null) continue;
                    if(info.getIsimg() ==1){
                        FileUtils.saveImgToLocal(YKjActivity.this,
                                Config.downloadUrl+"?filename="+info.getRealname(), true);
                    }else {
                        YKjActivity.this.downloadFolder(info.getId());
                    }
                }
            }
        });
    }

    //云空间图片点击事件，判断图片是文件夹还是图片
    //图片进行展示，文件夹进入下一目录
    // type 1.图片 2.文件夹
    @Override
    public void onClick(View v) {
        if(this.ykjAdapter.openSelected){ //打开了选择图片模式
            Log.d(TAG, "onClick: 选择图片模式");
            CheckBox cb = (CheckBox) v.getTag(R.id.sy_item_cb);
            cb.setChecked(!cb.isChecked());
        }else{
            Log.d(TAG, "onClick: 进入onclick");
            if(!(v instanceof ImageView)) return ;
            ImageView iv = (ImageView) v;
            Byte type = (Byte) iv.getTag(R.id.ykj_imgType); //获取图片类型 -文件或文件夹

            Log.d(TAG, "onClick: type="+type);

            int currentposition = (int)iv.getTag(R.id.ykj_imgPosiotn);//获取图片的位置
            ArrayList<String> imagePaths = getImagePaths();
            int realPosition = currentposition - (this.ykjAdapter.getData().size() - imagePaths.size());
            if(type.intValue() == 1){ //图片
                Intent intent = new Intent(this, DetailActivity.class);
                intent.putExtra("from",  this.getClass().getName());
                intent.putExtra("position", realPosition);
                intent.putStringArrayListExtra("data", imagePaths);
                this.startActivity(intent);
            }else if(type.intValue() == 2){ //文件夹

                Object id = iv.getTag(R.id.ykj_imgId);
                fidStack.add(id.toString());
                this.ykjAdapter = new YkjAdapter(this, Config.getFolderFile+"?fid="+id);
                this.rv.setAdapter(ykjAdapter);
                //this.ykjAdapter.changeFolder(Config.getFolderFile+"?fid="+id); //进入文件夹
            }
        }

    }

    //获取云空间中文件夹中详细的图片的图片路径
    private ArrayList<String> getImagePaths(){
        ArrayList<String> imagePaths = new ArrayList<>();
        List<ImageInfo> imageInfos = this.ykjAdapter.getData();
        for (ImageInfo info: imageInfos) {
            if(info.getIsimg().intValue() == 1){ //图片
                imagePaths.add(Config.getImg+"?imgName="+info.getRealname());
            }
        }
        return imagePaths;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 按下BACK，同时没有重复
            if(fidStack.size() >0){
                fidStack.remove(fidStack.size()-1);
                if(fidStack.size() == 0){
                    this.ykjAdapter = new YkjAdapter(this, Config.getFolderFile);
                    this.rv.setAdapter(ykjAdapter);
                    //this.ykjAdapter.changeFolder(Config.getFolderFile);
                }else{
                    this.ykjAdapter = new YkjAdapter(this, Config.getFolderFile+"?fid="+fidStack.get(fidStack.size()-1));
                    this.rv.setAdapter(ykjAdapter);
                    //this.ykjAdapter.changeFolder(Config.getFolderFile+"?fid="+fidStack.get(fidStack.size()-1));
                }
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void llSetVISIBLE(){
        this.ll.setVisibility(View.VISIBLE);
    }

}
