package com.google.codelab.mlkit;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.service.autofill.UserData;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_CLIENT;
import static android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
import static android.speech.SpeechRecognizer.ERROR_NETWORK;
import static android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
import static android.speech.SpeechRecognizer.ERROR_SERVER;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
import static com.google.codelab.mlkit.App.CHANEL_ID;

public class MyServie extends RecognitionService {
    public static final int MSG_VOICE_RECO_READY = 0;
    public static final int MSG_VOICE_RECO_END = 1;
    public static final int MSG_VOICE_RECO_RESTART = 2;
    private SpeechRecognizer mSrRecognizer;
    boolean mBoolVoiceRecoStarted;
    protected AudioManager mAudioManager;
    Intent itIntent;//???????????? Intent
    boolean end = false;
//RecognitionService??? ??????????????? Service ??????????????? ??? ????????? ????????? ????????? ????????? ??????????????? ?????????

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Mute
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            }
        }else {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
        //??????????????? ???????????? ????????? ????????? ???????????????
        if (SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
            itIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            itIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            itIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toString());
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            itIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            startListening();
        }

    }

    private Handler mHdrVoiceRecoState = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VOICE_RECO_READY:
                    break;
                case MSG_VOICE_RECO_END: {
                    stopListening();
                    sendEmptyMessageDelayed(MSG_VOICE_RECO_RESTART, 1000);
                    break;
                }
                case MSG_VOICE_RECO_RESTART:
                    startListening();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this
                , 0, notificationIntent, 0); //????????? ????????? ??? ?????? ???????????????

        Notification notification = new NotificationCompat.Builder(this, CHANEL_ID)
                .setContentTitle("Service")
                .setContentText("???????????? ???")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

       // startForeground(1, notification);

        return START_STICKY;
    }

/*
    @Override
    public void onDestroy() {
        super.onDestroy();
        end = true;
        mSrRecognizer.destroy();
        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_READY); //???????????? ????????? ?????? ??????
        if (mAudioManager != null)
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
    }

 */


    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {

    }


    public void startListening() {
        if (!end) {
            //??????????????? ???????????? ?????? Mute
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {

                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                }
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }


            if (!mBoolVoiceRecoStarted) { // ????????? ??????????????? ????????? ????????? ?????? ?????? ????????? ???????????? ??? ???
                if (mSrRecognizer == null) {
                    mSrRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    mSrRecognizer.setRecognitionListener(mClsRecoListener);
                }
                mSrRecognizer.startListening(itIntent);
            }
            mBoolVoiceRecoStarted = true;  //???????????? ????????? ?????? ???
        }
    }

    public void stopListening() //Override ????????? ?????? ????????? ???????????? ?????? ??????????????? ????????? ???
    {
        try {
            if (mSrRecognizer != null && mBoolVoiceRecoStarted) {
                mSrRecognizer.stopListening(); //???????????? Override ????????? ??????
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mBoolVoiceRecoStarted = false;  //???????????? ??????
    }


    @Override
    protected void onCancel(Callback listener) {
        mSrRecognizer.cancel();
    }

    @Override
    protected void onStopListening(Callback listener) { //???????????? Override ????????? ????????????
        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_RESTART); //???????????? ????????? ?????? ??????
    }

    private RecognitionListener mClsRecoListener = new RecognitionListener() {
        @Override
        public void onRmsChanged(float rmsdB) { //????????? ???????????? ????????? ????????? ??????
            Log.d("sound",""+rmsdB+"dB");
            //???????????? ????????? ????????? ?????????
            //???????????? ????????? UI?????? ?????????????????? ????????? ??? ??????
            if(rmsdB>30)
            {
                Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public void onResults(Bundle results) {
            //Recognizer KEY??? ???????????? ????????? ???????????? ???????????? ??????
            /*
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            final String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            Log.d("key", Arrays.toString(rs));

             */
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int intError) {

            switch (intError) {

                case ERROR_NETWORK_TIMEOUT:
                    //???????????? ????????????
                    break;
                case ERROR_NETWORK:
                    break;

                case ERROR_AUDIO:
                    //?????? ??????
                    break;
                case ERROR_SERVER:
                    //???????????? ????????? ??????
                    break;
                case ERROR_CLIENT:
                    //??????????????? ??????
                    break;
                case ERROR_SPEECH_TIMEOUT:
                    //?????? ????????? ?????? ????????? ???
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    break;
                case ERROR_NO_MATCH:
                    //????????? ????????? ?????? ????????? ???
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);

                    break;
                case ERROR_RECOGNIZER_BUSY:
                    //RecognitionService??? ?????? ???
                    break;
                case ERROR_INSUFFICIENT_PERMISSIONS:
                    //uses - permission(??? RECORD_AUDIO) ??? ?????? ???
                    break;

            }
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) { //?????? ????????? ?????? ?????? ???

        }
    };


}