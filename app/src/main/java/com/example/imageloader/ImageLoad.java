package com.example.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoad {

    private final static int SET_IMAGE = 0;//Message.what
    private static volatile ImageLoad imageLoad = null;//单例模式
    //图片参数
    private String urlPath = null;
    private ImageView imageView = null;
    private Bitmap bitmap = null;
    private int maxKb = -1;

    private Context context = null;

    //线程管理
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SET_IMAGE:
                    imageView.setImageBitmap(bitmap);
                    Log.d("image********", String.valueOf(imageView.hashCode()));
                    Log.d("image********", "设置图片成功");
                    break;
            }
        }
    };

    private ImageLoad() {
    }

    public static ImageLoad with(Context context) {
        if (imageLoad == null) {
            synchronized (ImageLoad.class) {
                if (imageLoad == null) {
                    imageLoad = new ImageLoad();
                }
            }
        }
        imageLoad.context = context;
        return imageLoad;
    }

    public ImageLoad load(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    public ImageLoad applyQuality(int maxKb) {
        this.maxKb = maxKb;
        return this;
    }

    public void into(final ImageView imageView) {
        this.imageView = imageView;
        Log.d("image********into", imageView.hashCode() + "");
        setImage();
    }

    private void setImage() {
        if (maxKb == -1) {
            originalImage();//原图
        } else {
            compressByQuality();//质量压缩图
        }
    }

    private void originalImage() {
        HttpPool.getInstance().getFixedPool(5).execute(new Runnable() {
            @Override
            public void run() {
                getOriginalBitmap();
//                Message message = Message.obtain();
//                message.what = SET_IMAGE;
//                handler.sendMessage(message);
            }
        });
    }

    private void compressByQuality() {
        HttpPool.getInstance().getFixedPool(5).execute(new Runnable() {
            @Override
            public void run() {
                int options = 99;//图片质量
                getOriginalBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                while (baos.toByteArray().length / 1024 > maxKb) {
                    baos.reset();//清空baos
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
                    options--;//画质减少
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                bitmap = BitmapFactory.decodeStream(bais);
                Message message = Message.obtain();
                message.what = SET_IMAGE;
                handler.sendMessage(message);
            }
        });
    }

    private void getOriginalBitmap() {
        URL imageUrl = null;
        try {
            imageUrl = new URL(imageLoad.urlPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (imageUrl != null) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) imageUrl.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                Message message = Message.obtain();
                message.what = SET_IMAGE;
                handler.sendMessage(message);
            }
        }
    }

}
