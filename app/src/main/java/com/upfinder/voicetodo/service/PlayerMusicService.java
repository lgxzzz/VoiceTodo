package com.upfinder.voicetodo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.upfinder.voicetodo.MyApplication;
import com.upfinder.voicetodo.R;
import com.upfinder.voicetodo.utils.UtilsKt;

import java.util.Random;
import java.util.logging.Logger;

public class PlayerMusicService extends Service {
    private final static String TAG = PlayerMusicService.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    public static boolean isAlive;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isAlive = true;
        UtilsKt.logE(TAG, TAG + "启动服务");
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent);
        mMediaPlayer.setLooping(true);
        startForeground(new Random().nextInt(10000),MyApplication.notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayMusic();
            }
        }).start();
        return START_STICKY;
    }

    private void startPlayMusic() {
        if (mMediaPlayer != null) {
            UtilsKt.logE(TAG, "启动后台播放音乐");
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            UtilsKt.logE(TAG, "关闭后台播放音乐");
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        isAlive = false;
        UtilsKt.logE(TAG, TAG + "停止服务");
        // 重启自己
        startWork(getApplicationContext());
    }
    public  static void startWork(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), PlayerMusicService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}