package com.xiaohongshu.audiorecord;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by wupengjian on 18/2/25.
 */
public class AudioTrackHelper {

    private AudioTrack mAudioTrack;
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

    private TrackThread mTrackThread;

    public AudioTrackHelper() {

        //AudioAttributes是一个封装音频各种属性的类
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder()
                //设置音频流的合适属性（音乐）
                .setLegacyStreamType(AudioAttributes.CONTENT_TYPE_MUSIC)
                //设置音频使用场景（多媒体）
                .setUsage(AudioAttributes.USAGE_MEDIA);
        AudioAttributes attributes = attrBuilder.build();

        AudioFormat.Builder formatBuilder = new AudioFormat.Builder()
                //设置采样率
                .setSampleRate(sampleRateInHz)
                //设置数据位宽，也叫编码制式和采样大小
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT);
        AudioFormat format = formatBuilder.build();

        mAudioTrack = new AudioTrack(attributes
                , format
                , bufferSizeInBytes
                , AudioTrack.MODE_STREAM
                , AudioManager.AUDIO_SESSION_ID_GENERATE);
    }

    public void start(File targetFile) {
        if (targetFile == null || targetFile.isDirectory() || !targetFile.exists()) {
            return;
        }
        stop();
        mTrackThread = new TrackThread(mAudioTrack, bufferSizeInBytes, targetFile);
        mTrackThread.startPlay();
    }

    public void stop() {
        if (mTrackThread != null) {
            mTrackThread.stopPlay();
        }
    }

    class TrackThread extends Thread {

        private File targetFile;
        private AudioTrack audioTrack;
        private int bufferSizeInBytes;
        private boolean isPlaying = false;

        public TrackThread(AudioTrack audioTrack, int bufferSizeInBytes, File targetFile) {
            this.targetFile = targetFile;
            this.audioTrack = audioTrack;
            this.bufferSizeInBytes = bufferSizeInBytes;
        }

        public void startPlay() {
            isPlaying = true;
            audioTrack.play();
            start();
        }

        public void stopPlay() {
            isPlaying = false;
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.stop();
//                audioTrack.release();
            }
        }

        @Override
        public void run() {
            if (targetFile == null || targetFile.isDirectory() || !targetFile.exists()) {
                return;
            }
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(targetFile)));

                short[] buffer = new short[bufferSizeInBytes];
                while (isPlaying && dis.available() > 0) {

                    for (int i = 0; dis.available() > 0 && i < buffer.length; i++) {
                        buffer[i] = dis.readShort();
                    }
                    audioTrack.write(buffer, 0, buffer.length);
                }
                audioTrack.flush();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stopPlay();
                if (dis != null) {
                    try {
                        dis.close();
                        dis = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
