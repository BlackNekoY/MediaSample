package com.example.slimxu.mediasample.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.example.slimxu.mediasample.utils.MediaUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by slimxu on 2017/11/21.
 */

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";
    /**
     * 采样频率
     */
    private static final int RECORDER_RATE = 44100;
    /**
     * AudioFormat.CHANNEL_IN_MONO 单声道 所有设备都支持
     * AudioFormat.CHANNEL_IN_STEREO 双声道
     */
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 音频编码格式
     * AudioFormat.ENCODING_PCM_16BIT 所有设备都支持
     */
    private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * 缓冲区大小，这里使用 2*1024
     */
    private static final int BUFFER_ELEMENT_2_REC = 1024;
    private static final int BYTE_PER_ELEMENT = 2;

    private static final String OUTPUT_PATH = Environment.getExternalStorageDirectory() + "/MediaSample/Audio";
    private String mPcmFilePath;
    private String mWavFilePath;

    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private boolean isRecording;

    public void startRecording() {
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_FORMAT);

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_RATE,RECORDER_CHANNELS,
                RECORDER_AUDIO_FORMAT, BUFFER_ELEMENT_2_REC * BYTE_PER_ELEMENT);

        File file = new File(OUTPUT_PATH);
        if(!file.exists()) {
            file.mkdirs();
        }
        mPcmFilePath = OUTPUT_PATH + "/voice_" + System.currentTimeMillis() + ".pcm";
        mWavFilePath = mPcmFilePath.replace(".pcm", ".wav");

        mAudioRecord.startRecording();
        isRecording = true;
        new RecordThread().start();
    }

    public void stopRecording() {
        if(mAudioRecord != null) {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public String getWavFilePath() {
        return mWavFilePath;
    }

    private class RecordThread extends Thread {
        @Override
        public void run() {
            writeAudioDataToFile();
        }
    }

    private void writeAudioDataToFile() {
        short[] shortDatas = new short[BUFFER_ELEMENT_2_REC];
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(mPcmFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(fos == null) {
            Log.e(TAG, "Path:" + mPcmFilePath + ", not found.");
            return;
        }

        while (isRecording) {
            mAudioRecord.read(shortDatas, 0, BUFFER_ELEMENT_2_REC);
            byte[] byteDatas = short2byte(shortDatas);
            try {
                fos.write(byteDatas, 0, BUFFER_ELEMENT_2_REC * BYTE_PER_ELEMENT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long sampleRate = RECORDER_RATE;    // 采样率
        int channels = RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO? 1 : 2;
        int bitsPerSample = 16; // 采样点大小
        MediaUtil.pcm2wav(mPcmFilePath, mWavFilePath, mBufferSize, sampleRate, channels, bitsPerSample);
    }

    private byte[] short2byte(short[] shortDatas) {
        int shortArrsize = shortDatas.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (shortDatas[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (shortDatas[i] >> 8);
            shortDatas[i] = 0;
        }
        return bytes;
    }

}
