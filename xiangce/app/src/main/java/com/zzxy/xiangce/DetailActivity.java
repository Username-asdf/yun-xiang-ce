package com.zzxy.xiangce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zzxy.xiangce.utils.ImagesDataUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static String TAG = DetailActivity.class.getName();
    private ImageView iv;
    private ViewPager2 viewpager;
    private ArrayList<String> imageData;
    private int currPosition;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String from = intent.getStringExtra("from");
        if(from.equals(YKjActivity.class.getName())){ //查看图片详情来自YkjActivity
            imageData = intent.getStringArrayListExtra("data");
        }else if(from.equals(MyRecyclerViewAdapter.class.getName())){ //查看图片详情来自SYFragment
            //获取图片数据
            imageData = ImagesDataUtils.getImgData(this);
        }
        //this.imageData = (ArrayList<String>) intent.getSerializableExtra("data");
        //this.imageData = intent.getStringArrayListExtra("data");
        this.currPosition = intent.getIntExtra("position", 0);

//        Log.d(TAG, "onCreate: size:"+imageData.size());
        Log.d(TAG, "onCreate: position:"+currPosition);

//        String p1 = "/storage/emulated/0/DCIM/Camera/IMG_20220209_153050.jpg";
//        String p2 = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20220210-090348.jpg";
//        String p3 = "/storage/emulated/0/DCIM/Camera/IMG_20220209_153113.jpg";
//        ArrayList<String> temp = new ArrayList<>();
//        temp.add(p1);
//        temp.add(p2);
//        temp.add(p3);




        this.iv = findViewById(R.id.detail_iv);
        this.viewpager = findViewById(R.id.detail_viewpager2);
        this.viewpager.setAdapter(new DetailViewpagerAdapter(imageData, from));
        this.viewpager.setCurrentItem(currPosition, false);
        this.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "onPageScrolled: poition:"+position+"  positionOffset:"+positionOffset+"  positionOffsetPixels:"+positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged: state:"+state);
            }
        });
    }

}
