package com.example.slimxu.mediasample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.slimxu.mediasample.audio.AudioRecordActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoAudioRecordActivity(View view) {
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }
}
