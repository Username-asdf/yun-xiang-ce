package com.zzxy.xiangce;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.zzxy.xiangce.utils.Config;
import com.zzxy.xiangce.utils.HttpUtils;

import java.io.File;
import java.util.List;

public class DetailViewpagerAdapter extends RecyclerView.Adapter<DetailViewpagerAdapter.DetailViewHolder>{

    private List<String> images;
    private String from;
    DetailViewpagerAdapter(List<String> images, String from) {
        this.images = images;
        this.from = from;
    }

    public void setImages(List<String> images){
        this.images = images;
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return images.get(position).hashCode();
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        if(from.equals(YKjActivity.class.getName())){ //查看图片详情来自云空间
            GlideUrl path =  new GlideUrl(images.get(position),
                    HttpUtils.getHeader());

            Glide.with(holder.iv)
                    .load(path)
                    .into(holder.iv);
        }else if(from.equals(MyRecyclerViewAdapter.class.getName())){
            Glide.with(holder.iv).load(new File(images.get(position))).into(holder.iv);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class DetailViewHolder extends RecyclerView.ViewHolder{

        private ImageView iv;
        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            this.iv = itemView.findViewById(R.id.detail_iv);
        }
    }
}
