package com.example.slimxu.mediasample.audio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.slimxu.mediasample.R;

/**
 * Created by slimxu on 2017/11/21.
 */

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private AudioRecorder mRecorder = new AudioRecorder();
    private boolean mIsRecording = false;
    private Button mStartRecordBtn;
    private Button mPlayBtn;

    private AudioPlayer mPlayer = new AudioPlayer();
    private boolean mIsPlaying = false;

    private static final int AUDIO_PERMISSION_REQUEST_CODE = 1;
    private static final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 2;
    private boolean mHasRecordAudioPermission = false;
    private boolean mHasWriteSdcardPermission = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        mStartRecordBtn = findViewById(R.id.start_record);
        mPlayBtn = findViewById(R.id.play);

        mStartRecordBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);

        mHasRecordAudioPermission = checkAppPermission(Manifest.permission.RECORD_AUDIO, AUDIO_PERMISSION_REQUEST_CODE);
        mHasWriteSdcardPermission = checkAppPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_SDCARD_PERMISSION_REQUEST_CODE);
    }


    private boolean checkAppPermission(String permission, int requestCode) {
        if(ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            }else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
            return false;
        }
        return true;
    }

    private void startRecord() {
        if(mIsRecording) {
            mRecorder.stopRecording();
            mStartRecordBtn.setText("开始录音");
        }else {
            mRecorder.startRecording();
            mStartRecordBtn.setText("停止录音");
        }
        mIsRecording = !mIsRecording;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                if(mHasRecordAudioPermission) {
                    startRecord();
                }else {
                    Log.d("AudioRecordActivity", "permission denied.");
                }
                break;
            case R.id.play:
                startPlay();
                break;
        }
    }

    private void startPlay() {
        if(mIsPlaying) {
            mPlayBtn.setText("播放");
            mPlayer.stopPlay();
        }else{
            mPlayer.startPlay(mRecorder.getWavFilePath());
            mPlayBtn.setText("停止");
        }
        mIsPlaying = !mIsPlaying;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHasRecordAudioPermission = true;
                } else {
                    mHasRecordAudioPermission = false;
                }
                break;
            case WRITE_SDCARD_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHasWriteSdcardPermission = true;
                } else {
                    mHasWriteSdcardPermission = false;
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
