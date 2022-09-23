package com.google.codelab.mlkit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

public class SettingActivity extends AppCompatActivity {

    Switch switchV;
    Switch switchVib;
    TextView switchVib2;
    Switch switchN;
    Vibrator vibrator;
    View view;
    boolean a=true;
    private SharedPreferences appData;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        view=findViewById(R.id.intensity_setting_layout);
        SeekBar seekBar=findViewById(R.id.seekBar);
        switchV=findViewById(R.id.switch_video);
        switchN=findViewById(R.id.switch_notice);
        switchVib=findViewById(R.id.switch_vibration);//진동 버튼
        switchVib2=findViewById(R.id.switch_vibration_intensity);//진동세기버튼

        vibrator= (Vibrator)getSystemService(VIBRATOR_SERVICE); //Vibrator 객체 얻어오기
        switchV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        //설정값 불러오기
        appData = getSharedPreferences("appData", MODE_PRIVATE);

        //자동 알림 버튼
        switchN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchN.isChecked()) {//Log.d("sitch_notice", "자동 음성인식 활성화");
                    startService(new Intent(SettingActivity.this, MyServie.class));
                    Toast toast = Toast.makeText(getApplicationContext(), "자동 음성인식 활성화", Toast.LENGTH_SHORT);
                    toast.show();
                    save();//값 저장
                }
                else{
                    //Log.d("sitch_notice", "자동 음성인식 비활성화");
                    Toast toast = Toast.makeText(getApplicationContext(), "자동 음성인식 비활성화",Toast.LENGTH_SHORT);
                    toast.show();
                    switchN.setChecked(false);
                }

            }
        });

        //진동 버튼
        switchVib.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
              if (switchVib.isChecked()){
                  view.setVisibility(View.VISIBLE);
              }else{
                  view.setVisibility(View.GONE);
              }
            }
        });

        //진동세기 조절 seekBar
        // 1) 패턴
        long[] pattern = {800, 1000, 800, 1000}; // 800ms 대기, 1000ms 진동
        // 2) 강도
        int[] amplitudes = {0, 100, 0, 200}; // 대기, 100강도, 대기, 200 강도
        // 3) VibrationEffect 생성(-1: 반복X)
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, -1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekBar 조작 중
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //seekBar 처음 터치시
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vibrator.vibrate(vibrationEffect);
            }
        });
    }

//switch 설정값 저장
public void save(){
        SharedPreferences.Editor editor=appData.edit();//에디터 객체
    editor.putBoolean("save_switch",switchN.isChecked());//저장시킬 이름, 저장시킬 값
    editor.commit();//commit
    editor.apply();
}

//switch 설정값 불러오기
    public void load(){

    }
}
