package com.example.fragmentbestpractice.uavhover;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.usrcloudlibrary.USRConnect;
import com.android.usrcloudlibrary.USRConnectListener;
import com.android.usrcloudlibrary.USRConnectManager;
import com.android.usrcloudlibrary.Utils.USRConfig;
import com.android.usrcloudlibrary.Utils.USRStringUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mWebsite;
    private EditText mPassward;
    private Button mLink;
    private USRConnectManager connectManager;
    private USRConnect connect;
    private boolean isConnect;
    private boolean isError;

    private TextView tvContent;//收到的内容

    private String  did;//设备ID
    private  String compass;//通信密码

    final String DEFAULT_DID = "00006737000000000001";//设定的设备ID
    final String DEFAULT_COMPASS = "0000peng";//云端设定的通信密码

    final String mstrWebsite = "www.UAVHover.com";//域名
    final String mstrPassward = "1061239743";//密码

    //如果你有多个连接，可以用个list保存
//    private List<USRConnect> connects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mWebsite = (EditText)findViewById(R.id.ip_num);
        mPassward = (EditText)findViewById(R.id.port_num);
        mLink = (Button)findViewById(R.id.link);

        mWebsite.setText(mstrWebsite);
        mPassward.setText(mstrPassward);

        tvContent = (TextView) findViewById(R.id.tv_content);
        connectManager = USRConnectManager.getInstance();
        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLink.setOnClickListener(this);


    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.link:
                String strWebsite = mWebsite.getText().toString();
                String strPassward = mPassward.getText().toString();
//                Toast.makeText(LoginActivity.this, strWebsite + strPassward,
//                        Toast.LENGTH_SHORT).show();
                if(strWebsite.equals(mstrWebsite) && strPassward.equals(mstrPassward)){

                    System.out.print(strWebsite + strPassward);
                    connect();
                }
                break;
            default:
//                if (connect != null && isConnect){
//                    connect.send(etSend.getText().toString().getBytes());
//                }
                break;
        }
    }


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
            Toast.makeText(this,"请输入设备id",Toast.LENGTH_SHORT).show();
            return;
        }

        compass = DEFAULT_COMPASS;
        if (TextUtils.isEmpty(compass)){
            Toast.makeText(this,"请输入通信密码",Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(LoginActivity.this, did + compass,
                        Toast.LENGTH_SHORT).show();
        isError = false;//初始化为false
        //创建一个连接
        connect = connectManager.createConnect(did,compass, USRConfig.CLOUD_TEMP_PORT, new USRConnectListener() {
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


                Toast.makeText(LoginActivity.this,error,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onRegisterSuccess(String deviceId) {
//                isError = true;
                System.out.println("连接成功");
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, uavActivity.class);
                startActivity(intent);
                System.out.println("注册成功------------------->deviceId:"+deviceId);
            }

            @Override
            public void onConnectSuccess(String deviceId) {
//                System.out.println("连接成功");
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, uavActivity.class);
//                startActivity(intent);
                isConnect = true;
//                isError = false;
                mLink.setText("断开");
            }



            @Override
            public void onConnectBreak(String deviceId) {
                System.out.println("连接断开--------------->devId:"+deviceId);
                isConnect = false;
                mLink.setText("连接");
            }

            @Override
            public void onReceviceData(String deviceId, byte[] data) {
                System.out.println("deviceId------------>"+deviceId+"  data:"+ USRStringUtils.bytesToHexString(data));
                String s = new String(data);
                tvContent.append(s+"\n");
                tvContent.append(USRStringUtils.bytesToHexString(data)+"\n");
            }


        });

        //如果需要建立多个连接，可以通过MainActivity实现USRConnectListener，建立的连接共用一个USRConnectListener;
        //connect = connectManager.createConnect(devId,pasd,USRConfig.CLOUD_LONG_PORT, this) //this 是MainActivity实现了USRConnectListener
        //多个连接可以用list保存起来
        //list.add(connect);
    }
}
