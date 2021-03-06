package com.example.fragmentbestpractice.uavhover;

/**
 * Created by Administrator on 2017-03-16.
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.android.usrcloudlibrary.USRConnect;
import com.android.usrcloudlibrary.USRConnectListener;
import com.android.usrcloudlibrary.USRConnectManager;
import com.android.usrcloudlibrary.Utils.USRConfig;
import com.android.usrcloudlibrary.Utils.USRStringUtils;


/**
 * 实现View对象，响应陀螺仪改变事件
 * 达到类似IOS那种背景图片移动的效果
 * 在onResume里使用{@link #start()}
 * 在onPause里使用{@link #stop()}
 */
public class ParallelViewHelper implements GyroScopeSensorListener.ISensorListener {

    /**
     * 默认0.02f在宽度填满屏幕的图片上，移动起来看着很舒服
     */
    public static final float TRANSFORM_FACTOR = 0.02f;
    private float mTransformFactor = TRANSFORM_FACTOR;
    private View mParallelView;
    private float mCurrentShiftX;
    private float lastmCurrentShiftX;
    private float lastmCurrentShiftY;
    private float mCurrentShiftY;
    private float differY;
    private float differX;
    private GyroScopeSensorListener mSensorListener;
    private ViewGroup.LayoutParams mLayoutParams;
    private int mViewWidth;
    private int mViewHeight;
    private int mShiftDistancePX;

    //有人穿透云连接接口
    private USRConnectManager usrConnectManager;
    private String  did;//设备ID
    private  String compass;//通信密码
    private USRConnect connect;
    private boolean isConnect;


    final String DEFAULT_DID = "00006737000000000001";//设定的设备ID
    final String DEFAULT_COMPASS = "0000peng";//云端设定的通信密码

    public ParallelViewHelper(Context context, final View targetView) {
        this(context, targetView, context.getResources().getDimensionPixelSize(R.dimen.image_shift));
    }

    /**
     * 初始化一个
     *
     * @param context
     * @param targetView
     * @param shiftDistancePX
     */
    public ParallelViewHelper(Context context, final View targetView, int shiftDistancePX) {
        mShiftDistancePX = shiftDistancePX;
        mSensorListener = new GyroScopeSensorListener(context);
        mSensorListener.setSensorListener(this);
        mParallelView = targetView;
        mParallelView.setX(-mShiftDistancePX);
        mParallelView.setY(-mShiftDistancePX);
        mLayoutParams = mParallelView.getLayoutParams();
        mViewWidth = mParallelView.getWidth();
        mViewHeight = mParallelView.getHeight();
        //获取connectManager实例
        usrConnectManager = USRConnectManager.getInstance();
        connect();

        if (mViewWidth > 0 && mViewHeight > 0) {
            bindView();
            return;
        }

        ViewTreeObserver vto = targetView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                targetView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mViewWidth = targetView.getWidth();
                mViewHeight = targetView.getHeight();
                bindView();
            }
        });
    }

    void bindView() {
        mLayoutParams.width = mViewWidth + mShiftDistancePX * 2;
        mLayoutParams.height = mViewHeight + mShiftDistancePX * 2;
        mParallelView.setLayoutParams(mLayoutParams);
    }


    /**
     * 注册监听陀螺仪事件
     */
    public void start() {
        mSensorListener.start();
    }

    /**
     * 监听陀螺仪事件耗电，因此在onPause里需要注销监听事件
     */
    public void stop() {
        mSensorListener.stop();
    }

    /**
     * 设置移动的补偿变量，越高移动越快，标准参考{@link #TRANSFORM_FACTOR}
     *
     * @param transformFactor
     */
    public void setTransformFactor(float transformFactor) {
        mTransformFactor = transformFactor;
    }

    @Override
    public void onGyroScopeChange(float horizontalShift, float verticalShift) {
        mCurrentShiftX += mShiftDistancePX * horizontalShift * mTransformFactor;
        mCurrentShiftY += mShiftDistancePX * verticalShift * mTransformFactor;
        //记录上一次mCurrentShiftX，mCurrentShiftY的值，并做差，超过一定值则发送到STM32
        differY = lastmCurrentShiftY-mCurrentShiftY;
        differX = lastmCurrentShiftX-mCurrentShiftX;

        //Log.d("ParalleViewHelper",lastmCurrentShiftY+"、"+lastmCurrentShiftX);
        //Log.d("ParalleViewHelper","记录上次的Y和X"+lastmCurrentShiftY+"和"+lastmCurrentShiftX);
        //Log.d("ParalleViewHelper","记录Y和X的差"+differY+"和"+differX);
        if(Math.abs(differY)>2) {
            //Log.d("ParalleViewHelper", mShiftDistancePX + "和" + horizontalShift);
            Log.d("ParalleViewHelper",differY+"");
            String strDifferY = differY +"";
            byte[] b=strDifferY.getBytes();
            //发送数据给服务器
            connect.send(b);
        }

        if (Math.abs(mCurrentShiftX) > mShiftDistancePX)
            mCurrentShiftX = mCurrentShiftX < 0 ? -mShiftDistancePX : mShiftDistancePX;

        if (Math.abs(mCurrentShiftY) > mShiftDistancePX)
            mCurrentShiftY = mCurrentShiftY < 0 ? -mShiftDistancePX : mShiftDistancePX;

        //默认就margin 负的边距尺寸，因此 margin的最大值是 负的边距尺寸*2 ~ 0
        //mParallelView.setX((int) mCurrentShiftX - mShiftDistancePX);
        mParallelView.setY((int) mCurrentShiftY - mShiftDistancePX);
        lastmCurrentShiftX = mCurrentShiftX;
        lastmCurrentShiftY = mCurrentShiftY;
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


//                Toast.makeText(LoginActivity.this,error,Toast.LENGTH_SHORT).show();
                Log.d("ParallelViewHelper",error);
            }
            @Override
            public void onRegisterSuccess(String deviceId) {
                System.out.println("连接成功");
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, uavActivity.class);
//                startActivity(intent);
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
                System.out.println("deviceId------------>"+deviceId+"  data:"+ USRStringUtils.bytesToHexString(data));
                String s = new String(data);
                Log.d("ParallelViewHelper",s);
//                tvContent.append(s+"\n");
//                tvContent.append(USRStringUtils.bytesToHexString(data)+"\n");
            }


        });

        //如果需要建立多个连接，可以通过MainActivity实现USRConnectListener，建立的连接共用一个USRConnectListener;
        //connect = userConnectManager.createConnect(devId,pasd,USRConfig.CLOUD_LONG_PORT, this) //this 是MainActivity实现了USRConnectListener
        //多个连接可以用list保存起来
        //list.add(connect);
    }

}
