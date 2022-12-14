// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelab.mlkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.ByteArrayOutputStream;

//import android.file.Files.createFile;

public class mMainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Button mPhotoButton;
    private Button mCameraButton;
    private Bitmap pic;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    final int CAMERA = 100; // ????????? ????????? ???????????? ????????? ???
    final int GALLERY = 101; // ????????? ?????? ??? ???????????? ????????? ???
    Intent intent;

    //????????????
    SpeechRecognizer mRecognizer;
    ImageButton sttBtn;
    ImageButton sttTrnBtn;
    final int PERMISSION = 1;
    TextView textView;
    String strs;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat imageDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String imagePath;

    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmain);

        mCameraButton = findViewById(R.id.button_camera);
        mPhotoButton = findViewById(R.id.button_gallery);
        mCameraButton.setOnClickListener(this);
        mPhotoButton.setOnClickListener(this);
        //        ?????? ??????
        boolean hasCamPerm = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasWritePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasCamPerm || !hasWritePerm)  // ?????? ?????? ???  ???????????? ??????
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        getSupportActionBar().setTitle("");
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navi_view);
        navigationView.setNavigationItemSelectedListener(this);

        //???????????????
        textView = (TextView)findViewById(R.id.sttResult);
        sttBtn = (ImageButton) findViewById(R.id.sttStart);
        sttTrnBtn=(ImageButton)findViewById(R.id.stt_trn);

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        sttBtn.setOnClickListener(v -> {
            if ( Build.VERSION.SDK_INT >= 23 ){
                // ????????? ??????
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.RECORD_AUDIO},PERMISSION);
            }
            mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });

        sttTrnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Translation.class);
                intent.putExtra("out_text", strs);
                startActivity(intent);
            }
        });

    }


//????????????
private RecognitionListener listener = new RecognitionListener() {
    @Override
    public void onReadyForSpeech(Bundle params) {
        Toast.makeText(getApplicationContext(), "??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int error) {
        String message;

        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "????????? ??????";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "??????????????? ??????";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "????????? ??????";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "???????????? ??????";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "????????? ????????????";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "?????? ??? ??????";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RECOGNIZER??? ??????";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "????????? ?????????";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "????????? ????????????";
                break;
            default:
                message = "??? ??? ?????? ?????????";
                break;
        }

        Toast.makeText(getApplicationContext(), "????????? ?????????????????????. : " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        // ?????? ?????? ArrayList??? ????????? ?????? textView??? ????????? ?????????
        ArrayList<String> matches =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for(int i = 0; i < matches.size() ; i++){
            textView.setText(matches.get(i));
            strs = matches.get(i).toString();
        }

        //?????? ???????????? ??????(??????) ?????? ????????? ??? ?????? ????????? ????????? ?????? ????????? ??????
        String key = "";
        key = SpeechRecognizer.RESULTS_RECOGNITION;
        ArrayList<String> mResult = results.getStringArrayList(key);
        String[] rs = new String[mResult.size()];
        mResult.toArray(rs);
        //rs??? ????????? ?????? ?????? ????????? ??????.
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
};

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.favorite_item) {
            Intent intent=new Intent(this,FavoriteActivity.class);
            startActivity(intent);
            return true;

        }else if (id == R.id.studymode_item){

        }else if(id == R.id.settings_item){
            Intent intent=new Intent(this,SettingActivity.class);
            startActivity(intent);
            return true;
        }

        drawerLayout.closeDrawers();
        // item.setChecked(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // ?????? ?????? ?????? ????????? ???
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() { //???????????? ?????? ???
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        switch (view.getId()) {
            case R.id.button_camera: // ????????? ?????? ???
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
                break;

            case R.id.button_gallery: // ????????? ?????? ???
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY);
                break;

            case R.id.stt_trn: //???????????? ??? ??????
                intent = new Intent(this,MainActivity.class);
                intent.putExtra("stt_string",strs);
                startActivity(intent);
        }
    }

    @SuppressLint("SimpleDateFormat")
    File createImageFile() throws IOException {
//        ????????? ?????? ??????
        String timeStamp = imageDate.format(new Date()); // ????????? ????????? ????????? ?????? "yyyyMMdd_HHmmss"?????? timeStamp
        String fileName = "IMAGE_" + timeStamp; // ????????? ?????? ???
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(fileName, ".jpg", storageDir); // ????????? ?????? ??????
        imagePath = file.getAbsolutePath(); // ?????? ???????????? ????????????
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) { // ????????? ?????? ??????
            Bitmap bitmap = null;
            intent=new Intent(this,MainActivity.class);
            switch (requestCode) {
                case GALLERY:
                    // 1) ????????? ??????????????? ????????? ????????????
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        imagePath = cursor.getString(index);
                        cursor.close();
                    }
                    // 2) InputStream ?????? ????????? ????????????
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        pic = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();

                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        pic.compress(Bitmap.CompressFormat.JPEG,50,bs);
                        intent.putExtra("bimg",bs.toByteArray());
                        startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case CAMERA: // ???????????? ????????? ????????? ??????
                    Bundle extras = data.getExtras();
                    pic = (Bitmap) extras.get("data");

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // ????????? ?????? ??????. ??? ???????????? 1/inSampleSize ??? ?????????
                    intent.putExtra("img",pic);//pic????????? ??????
                    startActivity(intent);
                    break;
            }
        }
    }


}
