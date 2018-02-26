package com.xiaohongshu.demo4copy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class RecordVoiceActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder = null;

    private File mSoundFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice);
    }

    public void start(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        } else {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                //音频输入源
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //音频输出格式
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
                //音频编码格式
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            }

            mSoundFile = getStoreFile("xiaobing.mp3");
            if (mSoundFile != null) {
                //输出文件
                mRecorder.setOutputFile(mSoundFile.getAbsolutePath());
                try {
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File getStoreFile(String name) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return null;
        } else {
            File dir = new File(Environment.getExternalStorageDirectory(), "sounds");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File storeFile = new File(dir, name);
            if (!storeFile.exists()) {
                try {
                    storeFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return storeFile;
        }
    }

    public void stop(View view) {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void play(View view) {
        if (mSoundFile == null) {
            return;
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(mSoundFile.getAbsolutePath());
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mPlayer.start();
    }

    public void recordVideo(View view) {
        startActivity(new Intent(this, RecordVideoActivity.class));
    }

    public void recordVerticalVideo(View view) {
        startActivity(new Intent(this, RecordVerticalVideoActivity.class));
    }
}
