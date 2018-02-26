package com.xiaohongshu.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wupengjian on 18/2/25.
 */
public class AudioRecordHelper {

    private AudioRecord mRecord;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

    private RecordThread mRecordThread;

    public AudioRecordHelper() {
        mRecord = new AudioRecord(audioSource
                , sampleRateInHz
                , channelConfig
                , audioFormat
                , bufferSizeInBytes);
    }

    public void startRecord(File destFile) {
        if (destFile == null || destFile.isDirectory() || !destFile.exists()) {
            return;
        }
        stopRecord();
        mRecordThread = new RecordThread(mRecord, bufferSizeInBytes, destFile);
        mRecordThread.startRecord();
    }

    public void stopRecord() {
        if (mRecordThread != null) {
            mRecordThread.stopRecord();
            mRecordThread = null;
        }
    }

    class RecordThread extends Thread {

        private File storeFile;
        private AudioRecord audioRecord;
        private int bufferSizeInBytes;
        private boolean isRecording = false;

        public RecordThread(AudioRecord record, int bufferSizeInBytes, File storeFile) {
            this.audioRecord = record;
            this.bufferSizeInBytes = bufferSizeInBytes;
            this.storeFile = storeFile;
        }

        public void startRecord() {
            isRecording = true;
            audioRecord.startRecording();
            start();
        }

        public void stopRecord() {
            isRecording = false;
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
//                audioRecord.release();
            }
        }

        @Override
        public void run() {
            if (storeFile == null || storeFile.isDirectory() || !storeFile.exists()) {
                return;
            }
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));

                short[] buffer = new short[bufferSizeInBytes];

                while (isRecording) {
                    // 从bufferSize中读取字节，返回读取的short个数
                    int bufferReadResult = audioRecord.read(buffer, 0, buffer.length);
                    // 循环将buffer中的音频数据写入到OutputStream中
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                stopRecord();

                if (dos != null) {
                    try {
                        dos.close();
                        dos = null;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
