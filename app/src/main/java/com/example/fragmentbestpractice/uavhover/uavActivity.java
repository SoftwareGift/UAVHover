package com.example.fragmentbestpractice.uavhover;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2017-03-16.
 */

public class uavActivity extends AppCompatActivity {
    ParallelViewHelper parallelViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uav);
//        //这个是直接跳转到MainActivity
//        Intent intent = new Intent();
//        intent.setClass(MainActivity.this, LoginActivity.class);
//        startActivity(intent);
        parallelViewHelper = new ParallelViewHelper(this, findViewById(R.id.main_image_background));


    }

    @Override
    protected void onResume() {
        super.onResume();
        parallelViewHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        parallelViewHelper.stop();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        parallelViewHelper.stop();
    }
}
