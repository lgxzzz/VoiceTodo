package com.upfinder.voicetodo.keeplive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LiveService extends Service {

    public  static void toLiveService(Context pContext){
        Intent intent=new Intent(pContext, LiveService.class);
        pContext.startService(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 屏幕管理者的单例
        final ScreenManager screenManager = ScreenManager.getInstance(this);
        // 屏幕广播监听器
        ScreenBroadcastListener listener = new ScreenBroadcastListener(this);
        listener.registerListener(new ScreenBroadcastListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                // 屏幕打开时候，关闭一个像素的Activity
                Log.e("lgx","onScreenOn");
                screenManager.finishActivity();
            }

            @Override
            public void onScreenOff() {
                // 屏幕锁屏的时候，开启一个像素的SinglePixelActivity
                Log.e("lgx","onScreenOff");
                screenManager.startActivity();
            }
        });
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}