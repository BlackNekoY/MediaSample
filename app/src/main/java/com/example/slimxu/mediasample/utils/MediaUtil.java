package com.example.slimxu.mediasample.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by slimxu on 2017/11/30.
 */

public class MediaUtil {

    public static final String TAG = "MediaUtil";

    /**
     *
     * @param pcmFilePath pcm 文件路径
     * @param wavFilePath wav 文件路径
     * @param bufferSize  AudioRecorder buffersize
     * @param sampleRate 采样率
     * @param channels  声道数
     * @param bitsPerSample 采样点大小
     */
    public static void pcm2wav(String pcmFilePath, String wavFilePath, int bufferSize, long sampleRate, int channels, int bitsPerSample) {
        File pcmFile = new File(pcmFilePath);
        if(!pcmFile.exists() || pcmFile.isDirectory()) {
            return;
        }
        File wavFile = new File(wavFilePath);
        FileInputStream pcmInputStream = null;
        FileOutputStream wavOutputStream = null;
        byte[] datas = new byte[bufferSize];
        try {
            pcmInputStream = new FileInputStream(pcmFile);
            wavOutputStream = new FileOutputStream(wavFile);

            long audioLength = pcmInputStream.getChannel().size();
            long totalLength = audioLength + 36;    //不包括RIFF和WAV
            writeWaveFileHeader(wavOutputStream, audioLength, totalLength, sampleRate, channels, bitsPerSample);

            while(pcmInputStream.read(datas) != -1) {
                wavOutputStream.write(datas);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(pcmInputStream != null) {
                try {
                    pcmInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(wavOutputStream != null) {
                try {
                    wavOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 写wav文件头
     * 所有数据使用的Little-Endian存储，也就是说对其中的数据，低位字节在前，高位字节在后
     *
     * @param out wav文件的输出流
     * @param totalAudioLen pcm文件长度
     * @param totalDataLen wav文件长度
     * @param sampleRate 采样频率 一般使用44100
     * @param channels 通道
     * @param bitsPerSample 采样点大小
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long sampleRate, int channels, int bitsPerSample) throws IOException {
        // 每秒字节数
        long byteRates = sampleRate * channels * bitsPerSample / 8;

        byte[] header = new byte[44];
        // RIFF标志
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // 文件长度
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        // WAVE标志
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // fmt标志
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // WAV头大小，PCM方式为16
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // 编码方式，PCM方式为1
        header[20] = 1;
        header[21] = 0;
        // 通道数量 单声道为1 双声道为2
        header[22] = (byte) channels;
        header[23] = 0;
        // 采样率
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        // 每秒字节数 = 采样频率 * 采样快大小
        header[28] = (byte) (byteRates & 0xff);
        header[29] = (byte) ((byteRates >> 8) & 0xff);
        header[30] = (byte) ((byteRates >> 16) & 0xff);
        header[31] = (byte) ((byteRates >> 24) & 0xff);
        // 采样块大小= 声道数量 * 采样点大小 / 8
        header[32] = (byte) (channels * bitsPerSample / 8); // block align
        header[33] = 0;
        // 采样点大小
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        // data标志
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // 数据长度
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    public static WavInfo readHeader(InputStream wavStream) throws IOException{

        final int HEADER_SIZE = 44;

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();
        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means
        // Linear
        // PCM
        int channels = buffer.getShort();
        checkFormat(channels == 1 || channels == 2, "Unsupported channels: "
                + channels);
        int rate = buffer.getInt();
        checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);
        buffer.position(buffer.position() + 6);
        int bits = buffer.getShort();
        checkFormat(bits == 16, "Unsupported bits: " + bits);
        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            Log.d(TAG, "Skipping non-data chunk");
            int size = buffer.getInt();
            wavStream.skip(size);

            buffer.rewind();
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(rate, channels, dataSize);
    }

    private static void checkFormat(boolean result, String msg) {
        if(!result) {
            throw new IllegalStateException(msg);
        }
    }

    public static class WavInfo {
        public final int rate;
        public final int channels;
        public final int dateSize;

        public WavInfo(int rate, int channels, int dateSize) {
            this.rate = rate;
            this.channels = channels;
            this.dateSize = dateSize;
        }
    }
}
