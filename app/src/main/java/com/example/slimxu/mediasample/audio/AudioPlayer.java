package com.example.slimxu.mediasample.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.example.slimxu.mediasample.utils.MediaUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by slimxu on 2017/11/29.
 */

public class AudioPlayer {

    public static final String TAG = "AudioPlayer";

    private static final int RATE = 44100;
    private static final int CHANNELS = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack mTrack;
    private int mBufferSize;
    private int mPlayPerSize;   // 每一次循环的播放块大小

    private byte[] mVoiceDatas;
    private String mInputFilePath;

    private boolean mIsPlayStart;

    public boolean startPlay(String wavFilePath) {
        if(mIsPlayStart) {
            Log.d(TAG, "startPlay failed, AudioPlayer is already start.");
            return false;
        }
        if(!createAudioTrack(wavFilePath)) {
            return false;
        }
        mIsPlayStart = true;
        mInputFilePath = wavFilePath;
        new PlayThread().start();
        return true;
    }

    public void stopPlay() {
        mIsPlayStart = false;
        if(mTrack != null) {
            mTrack.stop();
        }
    }

    public void release() {
        stopPlay();
        mTrack.release();
        mTrack = null;
    }

    private boolean createAudioTrack(String wavFilePath) {
        if(mTrack != null) {
            release();
        }
        try {
            FileInputStream wavFileInputStream = new FileInputStream(new File(wavFilePath));
            MediaUtil.WavInfo wavInfo = MediaUtil.readHeader(wavFileInputStream);
            mVoiceDatas = new byte[wavInfo.dateSize];
            wavFileInputStream.read(mVoiceDatas, 0, mVoiceDatas.length);

            int rate = wavInfo.rate;
            int channel = wavInfo.channels == 1? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
            int format = AUDIO_FORMAT;

            mBufferSize = AudioTrack.getMinBufferSize(rate, channel, format);

            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate, channel, format, mBufferSize, AudioTrack.MODE_STREAM);
            mPlayPerSize = mBufferSize * 2;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private class PlayThread extends Thread {
        private int mOffset;
        @Override
        public void run() {
            mTrack.play();
            while (mIsPlayStart) {
                try {
                    int size = mTrack.write(mVoiceDatas, mOffset, mPlayPerSize);
                    mOffset += mPlayPerSize;
                    if(mOffset >= mVoiceDatas.length) {
                        break;
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stopPlay();
        }
    }

}
