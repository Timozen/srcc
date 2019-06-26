package com.srcc.cameraapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(null == savedInstanceState){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, CameraFragment.newInstance()).commit();
        }

    }
}
