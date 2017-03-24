package com.example.fragmentbestpractice.uavhover;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.usrcloudlibrary.USRConnect;
import com.android.usrcloudlibrary.USRConnectListener;
import com.android.usrcloudlibrary.USRConnectManager;
import com.android.usrcloudlibrary.Utils.USRConfig;
import com.android.usrcloudlibrary.Utils.USRStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static android.R.attr.start;
import static android.R.attr.width;
import static android.content.ContentValues.TAG;


/**
 * Created by Administrator on 2017-03-17.
 */

public class DrawActivity extends Activity {
    private ImageView iv;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    int startX = -1;
    int startY;
    int stopX = -1;
    int stopY;

    int widthPix;//运行时的分辨率，即本机分辨率
    int heightPix;
    private float baseScreenWidth = 480;  //开发时的分辨率
    private float baseScreenHeight = 800;


    //有人穿透云连接接口
    private USRConnectManager usrConnectManager;
    private String  did;//设备ID
    private  String compass;//通信密码
    private USRConnect connect;
    private boolean isConnect;


    final String DEFAULT_DID = "00006737000000000001";//设定的设备ID
    final String DEFAULT_COMPASS = "0000peng";//云端设定的通信密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        setContentView(R.layout.activity_draw);
//        new Thread(runnable).start();
        this.iv = (ImageView) this.findViewById(R.id.iv);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPix = dm.widthPixels;
        heightPix = dm.heightPixels;
//        Bitmap baseBitmapCv = Bitmap.createBitmap(widthPix, heightPix-100, Bitmap.Config.ARGB_8888);
//        Canvas cv = new Canvas(baseBitmapCv);  //根据bitamp创建画布
//        cv.scale(widthPix/baseScreenWidth,heightPix/baseScreenHeight);
//        this.canvas = cv;
//        this.baseBitmap = baseBitmapCv;
        // 创建一张空白图片
        baseBitmap = Bitmap.createBitmap(widthPix, heightPix-100, Bitmap.Config.ARGB_8888);
//        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        // 画布背景为灰色
        //canvas.drawColor(Color.GRAY);
        this.canvas.drawColor(Color.WHITE);
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为红色
        paint.setColor(Color.RED);
        // 宽度5个像素
        paint.setStrokeWidth(5);
        // 先将灰色背景画上
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        iv.setImageBitmap(baseBitmap);
        //获取connectManager实例
        usrConnectManager = USRConnectManager.getInstance();
        connect();

        iv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        // 获取手按下时的坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        send_data(startX,startY,true);
                        Log.d("手按下的时候的坐标",startX + "和" + startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取手移动后的坐标
                        stopX = (int) event.getX();
                        stopY = (int) event.getY();
                        Log.d("手移动后的坐标",stopX + "和" +stopY);
                        send_data(stopX,stopY,false);
                        // 在开始和结束坐标间画一条线
                        canvas.drawLine(startX, startY, stopX, stopY, paint);
                        // 实时更新开始坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        Log.d("实时更新开始坐标",startX + "和" + startY);
                        send_data(startX,startY,true);
                        iv.setImageBitmap(baseBitmap);
                        break;
                }
                return true;
            }
        });
    }
    public void save(View view) {
        try {
            File file = new File(Environment.getExternalStorageDirectory()+"/aDraw",
                    System.currentTimeMillis() + ".jpg");
            OutputStream stream = new FileOutputStream(file);

            baseBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
//            // 模拟一个广播，通知系统sdcard被挂载
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
//            intent.setData(Uri.fromFile(Environment
//                    .getExternalStorageDirectory()));
//            sendBroadcast(intent);
            Toast.makeText(this, "保存图片成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void send_data(int startX,int startY,boolean isStart){
        byte b[] = new byte[4];
        b[0] = (byte)(startX >> 8);
        b[1] = (byte)(startX & 0xff);
        b[2] = (byte)(startY >> 8);
        b[3] = (byte)(startY & 0xff);
//        if (isStart){
//            b[4] = 1 & 0x01;//start 0x01
//        }
//        else
//        {
//            b[4] = 0 & 0x01;//stop 0x00;
//        }

        connect.send(b);
    }
    //如果你有多个连接，可以用个list保存
//    private List<USRConnect> connects = new ArrayList<>();

    /**
     * 连接到服务器
     */
    private void connect(){
        if (isConnect){
            connect.breakConnect();
            return;
        }

        did = DEFAULT_DID;
        if(TextUtils.isEmpty(did)){
//            Toast.makeText(Context,"请输入设备id",Toast.LENGTH_SHORT).show();
            Log.d("ParallelViewHelper","请输入设备id");
            return;
        }


        compass = DEFAULT_COMPASS;
        if (TextUtils.isEmpty(compass)){
//            Toast.makeText(this,"请输入通信密码",Toast.LENGTH_SHORT).show();
            return;
        }

//        Toast.makeText(LoginActivity.this, did + compass,
//                Toast.LENGTH_SHORT).show();
        //创建一个连接
        connect = usrConnectManager.createConnect(did,compass, USRConfig.CLOUD_TEMP_PORT, new USRConnectListener() {
            @Override
            public void onError(String s, int errorCode) {
                String error ="";
                System.out.println("连接错误--------->" + s + " errorCode:" + errorCode);
                switch (errorCode) {
                    case 0x31:
                        System.out.println("error--------->注册包不合法");
                        error = "注册包不合法";
                        break;
                    case 0x32:
                        System.out.println("error--------->通讯密码错误");
                        error = "通讯密码错误";
                        break;
                    case 0x33:
                        System.out.println("error--------->设备不存在");
                        error = "设备不存在";
                        break;
                    case 0x34:
                        System.out.println("error--------->设备被顶掉");
                        error = "设备被顶掉";
                        break;
                    case 0x25:
                        System.out.println("error--------->设备不在线");
                        error = "设备不在线";
                        break;
                    case 0x26:
                        System.out.println("error--------->目标组不存在或无权限");
                        error = "目标组不存在或无权限";
                        break;
                    case 0x27:
                        System.out.println("error--------->临时会话类型错误");
                        error = "临时会话类型错误";
                        break;
                }


                Toast.makeText(DrawActivity.this,error,Toast.LENGTH_SHORT).show();
                Log.d("ParallelViewHelper",error);
                Intent intent = new Intent();
                intent.setClass(DrawActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            @Override
            public void onRegisterSuccess(String deviceId) {
                System.out.println("连接成功");
                Log.d("ParallelViewHelper","注册成功------------------->deviceId:"+deviceId);
                System.out.println("注册成功------------------->deviceId:"+deviceId);
            }

            @Override
            public void onConnectSuccess(String deviceId) {
                System.out.println("连接成功");
                isConnect = true;
//                mLink.setText("断开");
            }



            @Override
            public void onConnectBreak(String deviceId) {
                System.out.println("连接断开--------------->devId:"+deviceId);
                isConnect = false;
//                mLink.setText("连接");
            }

            @Override
            public void onReceviceData(String deviceId, byte[] data) {
                int mStartX;
                int mStartY;
                int mStopX;
                int mStopY;
                System.out.println("deviceId------------>"+deviceId+"  data:"+ USRStringUtils.bytesToHexString(data));
                String s = new String(data);
                Log.d("ParallelViewHelper",s);
                if(data[4] == 0x01){
                    //如果data[4]是0x01，表示start
                    mStartX = data[0] << 8;
                    startX = mStartX + data[1];
                    mStartY = data[2] << 8;
                    startY = mStartY + data[3];
                    Log.d("start",startX + "和" + startY);
                }
                else{
                    mStopX = data[0] << 8;
                    stopX = mStopX + data[1];
                    mStopY = data[2] << 8;
                    stopY = mStopY + data[3];
                    Log.d("stop",stopX + "和" + stopY);
                }
                //如果两个坐标的值都不是初始值
                if(stopX!=-1 && startX != -1){
                    // 在开始和结束坐标间画一条线
                    canvas.drawLine(startX, startY, stopX, stopY, paint);
                    iv.setImageBitmap(baseBitmap);
                }
            }



        });

        //如果需要建立多个连接，可以通过MainActivity实现USRConnectListener，建立的连接共用一个USRConnectListener;
        //connect = userConnectManager.createConnect(devId,pasd,USRConfig.CLOUD_LONG_PORT, this) //this 是MainActivity实现了USRConnectListener
        //多个连接可以用list保存起来
        //list.add(connect);
    }

}
