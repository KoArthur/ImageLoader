package com.example.imageloader;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private Context context = null;
    private String url = "http://img5.adesk.com/5c7519a3e7bce75e0214c2eb?imageMogr2/thumbnail/!720x1280r/gravity/Center/crop/720x1280";
    private ImageView tv_original = null;
    private ImageView tv_low = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        context = getApplicationContext();
        tv_original = findViewById(R.id.tv_original);
        tv_low = findViewById(R.id.tv_low);
        ImageLoad.with(context).load(url).into(tv_original);
        Log.d("image", tv_original.hashCode() + "           1");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d("sleep", "主线程睡了");
            e.printStackTrace();
        }
        ImageLoad.with(context).load(url).into(tv_low);
        Log.d("image", tv_low.hashCode() + "                2");
    }
}
