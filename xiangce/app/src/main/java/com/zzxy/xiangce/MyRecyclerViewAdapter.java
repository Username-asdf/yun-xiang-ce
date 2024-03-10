package com.zzxy.xiangce;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zzxy.xiangce.scan.FolderBean;
import com.zzxy.xiangce.scan.PhoneUtils_Picture;
import com.zzxy.xiangce.utils.ImagesDataUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder>
        implements View.OnLongClickListener,CompoundButton.OnCheckedChangeListener {

    private static String TAG = MyRecyclerViewAdapter.class.getName();
    private ArrayList<String> data;
    private Activity activity;
    private boolean openCheckBox = false;// 是否开启checkbox

    private Set<Integer> selectedCheckbox = new HashSet<>();
    //private List<CheckBox> selectedCheckbox = new ArrayList<>(); //被选择的checkbox组件
    private TMessage message;

    MyRecyclerViewAdapter(Activity activity){
        this.data = ImagesDataUtils.getImgData(activity);
        this.activity = activity;
        Log.d(TAG, "MyRecyclerViewAdapter: 123");
    }

    public void setMessage(TMessage message) {
        this.message = message;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sy_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: "+position);
        for(int i=0;i<4;i++){
            int currposition = position*4+i;
            if(currposition >= data.size()){
                holder.items.get(i).iv.setVisibility(View.GONE);
                continue;
            }
            String path = data.get(currposition);

            Log.d(TAG, "onBindViewHolder: "+data.get(currposition));

            //设置checkbox当前位置
            CheckBox cb = holder.items.get(i).cb;
            cb.setTag(R.id.sy_item_cb_position, currposition);
            if(this.selectedCheckbox.contains(currposition)){
                cb.setChecked(true);
            }else{
                cb.setChecked(false);
            }

            ImageView iv = holder.items.get(i).iv;

            iv.setOnLongClickListener(this);
            iv.setTag(R.id.sy_item_img_path, path);

            holder.position = position;
            Glide.with(activity).load(new File(path)).into(iv);
        }
    }

    @Override
    public int getItemCount() {
        int count = (int)Math.ceil(data.size() / 4.0);
        return count;
    }


    //设置图片长按事件，为上传照片做准备
    //长按开启复选框
    @Override
    public boolean onLongClick(View v) {
        if(openCheckBox){ //打开了多选状态，停止长按事件
            return false;
        }else{
            //打开 上传，取消 按钮
            message.sendMessage(1,null);

            ImageView iv = (ImageView) v;
            this.openCheckBox = true; //将选择状态打开 改变图片的点击事件
            //将该图片的复选框状态设置为 true
            CheckBox cb = (CheckBox) iv.getTag(R.id.sy_item_cb);
            cb.setVisibility(View.VISIBLE);
            cb.setChecked(true);
            Log.d(TAG, "onLongClick: "+this.openCheckBox);
            return true;
        }
    }

    //复选框转台改变事件
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged: ");
        CheckBox cb = (CheckBox) buttonView;
        View iv = (View) buttonView.getTag(R.id.sy_item_img); //通过CheckBox获取图片对象
        String imgPath= (String) iv.getTag(R.id.sy_item_img_path);//通过图片对象获取图片路径
        //将被选择的图片放到selectedImgs中
        if(isChecked){ //被选择 放进去图片路径
            cb.setVisibility(View.VISIBLE);
            this.selectedCheckbox.add((Integer) cb.getTag(R.id.sy_item_cb_position));
        }else{// 取消选择 删除图片路径
            cb.setVisibility(View.GONE);
            this.selectedCheckbox.remove(cb.getTag(R.id.sy_item_cb_position));
            isExitSelectImg();
        }
    }

    private void isExitSelectImg(){
        if(selectedCheckbox.size()==0){
            openCheckBox = false;
            //关闭 上传，取消按钮
            this.message.sendMessage(2, null);
        }
    }

    //获取所有被选择的图片
    public List<String> getAllSelectedImg(){
        List<String> paths = new ArrayList<>();
        for(Integer i : selectedCheckbox){
            paths.add(this.data.get(i));
        }
        return paths;
    }

    //退出图片选择模式
    public void quitSelectImg(){
        openCheckBox = false;
        selectedCheckbox.clear();
        this.notifyDataSetChanged();
        isExitSelectImg();
    }

    // 一个ImageView和CheckBox的组合
    private class Item{
        public ImageView iv;
        public CheckBox cb;

        public Item(ImageView iv, CheckBox cb){
            this.iv = iv;
            this.cb = cb;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public List<Item> items;
        public int position;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            items = new ArrayList<>();

            ImageView iv1 = (ImageView) itemView.findViewById(R.id.item1);
            ImageView iv2 = (ImageView) itemView.findViewById(R.id.item2);
            ImageView iv3 = (ImageView) itemView.findViewById(R.id.item3);
            ImageView iv4 = (ImageView) itemView.findViewById(R.id.item4);

            CheckBox cb1 = (CheckBox) itemView.findViewById(R.id.cb_item1);
            CheckBox cb2 = (CheckBox) itemView.findViewById(R.id.cb_item2);
            CheckBox cb3 = (CheckBox) itemView.findViewById(R.id.cb_item3);
            CheckBox cb4 = (CheckBox) itemView.findViewById(R.id.cb_item4);


            items.add(new Item(iv1,cb1));
            items.add(new Item(iv2,cb2));
            items.add(new Item(iv3,cb3));
            items.add(new Item(iv4,cb4));

            //设置图片的点击事件
            iv1.setOnClickListener(this);
            iv2.setOnClickListener(this);
            iv3.setOnClickListener(this);
            iv4.setOnClickListener(this);
            //设置复选框的状态改变事件
            cb1.setOnCheckedChangeListener(MyRecyclerViewAdapter.this);
            cb2.setOnCheckedChangeListener(MyRecyclerViewAdapter.this);
            cb3.setOnCheckedChangeListener(MyRecyclerViewAdapter.this);
            cb4.setOnCheckedChangeListener(MyRecyclerViewAdapter.this);

            //将图片与复选框进行关联
            iv1.setTag(R.id.sy_item_cb,cb1);
            iv2.setTag(R.id.sy_item_cb,cb2);
            iv3.setTag(R.id.sy_item_cb,cb3);
            iv4.setTag(R.id.sy_item_cb,cb4);
            //将复选框与图片进行关联
            cb1.setTag(R.id.sy_item_img, iv1);
            cb2.setTag(R.id.sy_item_img, iv2);
            cb3.setTag(R.id.sy_item_img, iv3);
            cb4.setTag(R.id.sy_item_img, iv4);
        }

        @Override
        public void onClick(View v) {
            if(MyRecyclerViewAdapter.this.openCheckBox){ //复选框打开状态
                CheckBox cb = (CheckBox)v.getTag(R.id.sy_item_cb);
                cb.setChecked(!cb.isChecked());
            }else{
                if(data == null || data.size() == 0 ) return;
                //图片点击事件 -- 查看图片
                int id = 0;
                switch(v.getId()){
                    case R.id.item1:
                        id = 0;
                        break;
                    case R.id.item2:
                        id = 1;
                        break;
                    case R.id.item3:
                        id = 2;
                        break;
                    case R.id.item4:
                        id = 3;
                        break;
                }
                int finalPosition = position*4 + id;
                if(finalPosition > data.size()) return;

                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("from", MyRecyclerViewAdapter.class.getName());
                //intent.putStringArrayListExtra("data", data);
                intent.putExtra("position", finalPosition);
//            intent.putExtra("data", data);
                activity.startActivity(intent);
            }
        }


    }
}
