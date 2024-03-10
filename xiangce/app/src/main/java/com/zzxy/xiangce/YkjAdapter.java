package com.zzxy.xiangce;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zzxy.xiangce.pojo.ImageInfo;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.FileUtils;
import com.zzxy.xiangce.utils.HttpUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class YkjAdapter extends RecyclerView.Adapter<YkjAdapter.YkjViewHolder>
        implements View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {

    private static String TAG = YkjAdapter.class.getName();
    private List<ImageInfo> imageInfos;
    private YKjActivity context;
    private RequestOptions options;

    //图片缓存目录
    private String catchPath ;

    private Set<Integer> selectedImages = new HashSet<>();//配选择的图片或文件夹
    public boolean openSelected = false;
   // private TMessage message;


    public YkjAdapter(YKjActivity context, String folder){
        this.context = context;
        this.imageInfos = new ArrayList();

        options = new RequestOptions();
        options.placeholder(R.mipmap.ic_folder);
        options.diskCacheStrategy(DiskCacheStrategy.ALL);
        options.skipMemoryCache(false);

        this. catchPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        initData(folder);
    }


    //public void setMessage(TMessage message){
        //this.message = message;
    //}

    public List<ImageInfo> getData(){
        return this.imageInfos;
    }

    public void setData(List<ImageInfo> info){
        this.imageInfos = info;
        //进行本地图片缓存


        for(ImageInfo i:info){
            if(new File(catchPath+"/"+i.getRealname()).exists())
                continue;
            FileUtils.saveImgToLocal(context,
                    Config.getSmallImg+"?imgName="+i.getRealname(),
                    false);
        }


        this.notifyDataSetChanged();
    }

    public void changeFolder(String folder){
        initData(folder);
    }

    //初始化数据， 访问后台，请求数据
    private void initData(String path) {

        FormBody formBody = new FormBody.Builder()
                .add("pageSize","9999")
                .add("pageNum", "1")
                .add("fid", "0")
                .build();
        HttpUtils.post(path, formBody, new Callback() {
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
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData((List<ImageInfo>) new Gson().fromJson(body, new TypeToken<List<ImageInfo>>() {
                        }.getType())); //通知数据变化
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public YkjViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ykj_item_layout, parent, false);
        return new YkjViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YkjViewHolder holder, int position) {
        int i=0;
        int currPosition = position * 3;

        while(i<3 && currPosition < imageInfos.size()){
            ImageInfo info = imageInfos.get(currPosition);

            ImageView imageView = holder.items.get(i).iv;
            holder.items.get(i).tv.setText(info.getImgname()); //设置相册文字
            CheckBox cb = holder.items.get(i).cb;
            cb.setTag(R.id.sy_item_cb_position, currPosition);
            if(selectedImages.contains(currPosition)){
                cb.setChecked(true);
            }else{
                cb.setChecked(false);
            }

            cb.setOnCheckedChangeListener(this);
            imageView.setOnLongClickListener(this);

            imageView.setOnClickListener(context); //设置图片点击事件
            imageView.setTag(R.id.ykj_imgType,info.getIsimg()); //当前图片的类型类型 2.文件夹 1.图片
            imageView.setTag(R.id.ykj_imgPosiotn,currPosition); //设置图片当前位置
            imageView.setTag(R.id.ykj_imgId, info.getId());
            //imageView.setTag((int)type);
            String imageName = info.getRealname();
            if(imageName != null && !imageName.equals("")){
                StringBuilder bulider = new StringBuilder();
//                GlideUrl path =  new GlideUrl(Config.getSmallImg+"/?imgName="+imageName,
//                        HttpUtils.getHeader());

                Glide.with(imageView)
                        .setDefaultRequestOptions(options)
                        .load(catchPath+"/"+imageName)
                        .into(imageView);
            }

            currPosition++;
            i++;
        }

        if(i!=3){ //最后没有循环完Config.getSmallImg+"?imgName="+imageInfos.get(currPosition).getRealname()
            for(;i<3;i++){
                holder.items.get(i).iv.setVisibility(View.GONE); //隐藏图片
                holder.items.get(i).tv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        int size = (int) Math.ceil(imageInfos.size() / 3.0);
        Log.d(TAG, "getItemCount: "+size);
        return size;
    }

    //图片长按事件 - 进入图片选择模式
    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick: YkjAdapter");
        if(this.openSelected){ //打开了选择模式直接返回
          return false;
        }else{ //打开选择模式
            CheckBox cb = (CheckBox) v.getTag(R.id.sy_item_cb);
            cb.setChecked(true);
            Log.d(TAG, "onLongClick: YkjAdapter");
            this.openSelected = true;
            //显示功能按钮
            context.llSetVISIBLE();
            return true;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged: ");
        CheckBox cb = (CheckBox) buttonView;
        ImageView iv = (ImageView) cb.getTag(R.id.sy_item_img);
        Integer position = (Integer) cb.getTag(R.id.sy_item_cb_position);
        if(isChecked){
            cb.setVisibility(View.VISIBLE);
            this.selectedImages.add(position);
        }else{
            cb.setVisibility(View.GONE);
            this.selectedImages.remove(position);
            //检查选择的图片是否为0
            isExitSelecte();
        }
    }

    private void isExitSelecte(){
        if(this.selectedImages.size() == 0){
            this.openSelected = false;
            //this.notifyDataSetChanged();
            //更换Adapter，清除数据
            //message.sendMessage(3, null);
            //notifyDataSetChangeed()和glide使用会出现部分图片无法加载的问题
            //this.notifyDataSetChanged();

        }
    }

    //退出选择模式
    public void quitSelected(){
        Log.d(TAG, "quitSelected: 退出选择模式");
        this.selectedImages.clear();
        isExitSelecte();
    }

    //获取所有被选择的图片
    public List<ImageInfo> getAllSelectedImage(){
        List<ImageInfo> images = new ArrayList<>();
        for(Integer i : selectedImages){
            images.add(this.getData().get(i));
        }
        return images;
    }


    private class Item{
        public ImageView iv;
        public CheckBox cb;
        public TextView tv;
        public Item(ImageView iv,CheckBox cb, TextView tv){
            this.iv = iv;
            this.cb = cb;
            this.tv = tv;
        }
    }


    public class YkjViewHolder extends RecyclerView.ViewHolder{

        public ArrayList<Item> items;

        public YkjViewHolder(@NonNull View itemView) {
            super(itemView);
            items = new ArrayList<>();

            ImageView iv1 = itemView.findViewById(R.id.ykj_item_iv1);
            ImageView iv2 = itemView.findViewById(R.id.ykj_item_iv2);
            ImageView iv3 = itemView.findViewById(R.id.ykj_item_iv3);

            CheckBox cb1 = itemView.findViewById(R.id.ykj_item_cb1);
            CheckBox cb2 = itemView.findViewById(R.id.ykj_item_cb2);
            CheckBox cb3 = itemView.findViewById(R.id.ykj_item_cb3);

            TextView tv1 = itemView.findViewById(R.id.ykj_item_tv1);
            TextView tv2 = itemView.findViewById(R.id.ykj_item_tv2);
            TextView tv3 = itemView.findViewById(R.id.ykj_item_tv3);


            iv1.setTag(R.id.sy_item_cb, cb1);
            iv2.setTag(R.id.sy_item_cb, cb2);
            iv3.setTag(R.id.sy_item_cb, cb3);

            cb1.setTag(R.id.sy_item_img, iv1);
            cb2.setTag(R.id.sy_item_img, iv2);
            cb3.setTag(R.id.sy_item_img, iv3);


//            iv1.setTag(2); //2代表文件夹， 1代表图片
//            iv2.setTag(2);
//            iv3.setTag(2);
            items.add(new Item(iv1,cb1,tv1));
            items.add(new Item(iv2,cb2,tv2));
            items.add(new Item(iv3,cb3,tv3));
        }
    }




}
