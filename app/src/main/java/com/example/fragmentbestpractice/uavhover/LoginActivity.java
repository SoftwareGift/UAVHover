package com.example.fragmentbestpractice.uavhover;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;





public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mWebsite;
    private EditText mPassward;
    private Button mLink;
    private Button mDraw;
    private Button mPlay;


    private boolean isLink;//验证域名和密码是否正确

//    private TextView tvContent;//收到的内容



    final String mstrWebsite = "www.UAVHover.com";//域名
    final String mstrPassward = "1061239743";//密码

    private int link_num = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mWebsite = (EditText)findViewById(R.id.ip_num);
        mPassward = (EditText)findViewById(R.id.port_num);
        mLink = (Button)findViewById(R.id.link);
        mDraw = (Button)findViewById(R.id.Draw);
        mPlay = (Button)findViewById(R.id.Play);
        mWebsite.setText(mstrWebsite);
        mPassward.setText(mstrPassward);

//        tvContent = (TextView) findViewById(R.id.tv_content);
//
//        //使文本可以滑动
//        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLink.setOnClickListener(this);
        mDraw.setOnClickListener(this);
        mPlay.setOnClickListener(this);


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

                    isLink = true;
                    System.out.print(strWebsite + strPassward);
                    link_num += 1;
//                    connect();
                }
                else {
                    Toast.makeText(LoginActivity.this, "请输入正确的域名和密码",
                            Toast.LENGTH_SHORT).show();
                }
                if (link_num%2==0) {
                    mLink.setText("断开");
                }
                else mLink.setText("连接");
                break;
            case R.id.Draw:
                if(isLink){
                    Intent intent1 = new Intent();
                    intent1.setClass(LoginActivity.this, DrawActivity.class);
                    startActivity(intent1);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "请链接服务器",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.Play:
                if (isLink){
                    Intent intent2 = new Intent();
                    intent2.setClass(LoginActivity.this, uavActivity.class);
                    startActivity(intent2);
                }
                else {
                    Toast.makeText(LoginActivity.this, "请链接服务器",
                            Toast.LENGTH_SHORT).show();
                }


            default:
//                if (connect != null && isConnect){
//                    connect.send(etSend.getText().toString().getBytes());
//                }
                break;
        }
    }



}
