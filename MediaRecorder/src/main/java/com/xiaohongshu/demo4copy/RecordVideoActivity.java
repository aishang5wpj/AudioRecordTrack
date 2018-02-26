package com.xiaohongshu.demo4copy;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by wupengjian on 18/1/20.
 */

public class RecordVideoActivity extends AppCompatActivity {

    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    private SurfaceView mSurfaceView;
    private File mStoreFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record_video);
        // 设置横屏显示
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        // 设置分辨率
        mSurfaceView.getHolder().setFixedSize(1920, 1280);
        // 设置该组件让屏幕不会自动关闭
        mSurfaceView.getHolder().setKeepScreenOn(true);
    }

    /***
     * 1280, 720 还有一些明文的数字，有些手机上不合适，会黑屏，崩溃，咋办
     * 有些手机的摄像头不支持这个大小，具体支持的大小你要用
     * List<Size> list = camera.getParameters().getSupportedVideoSizes(); 通过打印 list.height和List.width就知道了
     * @param view
     */
    public void start(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        } else {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                // 设置从摄像头采集图像
                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                // 设置视频文件的输出格式
                // 必须在设置声音编码格式、图像编码格式之前设置
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                // 设置声音编码的格式
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                // 设置图像编码的格式
                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mRecorder.setVideoSize(3840, 2160);
                // 每秒 4帧
                mRecorder.setVideoFrameRate(20);
                //设置预览界面
                mRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
                //旋转90度
                mRecorder.setOrientationHint(180);
                //设置输出文件
                mStoreFile = getStoreFile("maobuyi.mp4");
                if (mStoreFile != null) {
                    mRecorder.setOutputFile(mStoreFile.getAbsolutePath());
                    try {
                        mRecorder.prepare();
                        mRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void play(View view) {
        if (mStoreFile == null) {
            return;
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(mStoreFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mPlayer.setDisplay(mSurfaceView.getHolder());
        try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder.reset();
        }
    }
}
