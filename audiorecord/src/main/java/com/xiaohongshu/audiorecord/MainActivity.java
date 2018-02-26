package com.xiaohongshu.audiorecord;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private File mAudioFile;
    private AudioRecordHelper mRecordHelper;
    private AudioTrackHelper mTrackHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecordHelper = new AudioRecordHelper();
        mTrackHelper = new AudioTrackHelper();
    }

    public void startRecord(View view) {
        if (mAudioFile == null) {
            mAudioFile = getStoreFile("xuxiaobing.pcm");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            return;
        }
        mRecordHelper.startRecord(mAudioFile);
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

    public void stopRecord(View view) {
        mRecordHelper.stopRecord();
    }

    public void startPlay(View view) {
        if (mAudioFile == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return;
        }
        mTrackHelper.start(mAudioFile);
    }

    public void stopPlay(View view) {
        mTrackHelper.stop();
    }
}
