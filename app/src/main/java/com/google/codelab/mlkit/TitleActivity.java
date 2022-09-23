package com.google.codelab.mlkit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class TitleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        ImageView img = (ImageView) findViewById(R.id.imgV);
        Glide.with(this).load(R.drawable.navi).into(img);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(TitleActivity.this,mMainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000); //delayMills(3초) 후 스플래시 화면을 닫음
    }
}