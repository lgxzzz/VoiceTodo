package com.upfinder.voicetodo.worker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import com.upfinder.voicetodo.MyApplication;
import com.upfinder.voicetodo.service.PlayerMusicService;
import com.upfinder.voicetodo.utils.UtilsKt;

import java.util.Timer;
import java.util.TimerTask;

public class LiveJobService extends JobIntentService {
    static final int JOB_ID = 10111;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                count++;
                UtilsKt.logE("LiveJobService", "-" + count);
                if (!PlayerMusicService.isAlive) {
                    Log.e("LiveJobService", "服务done" + count);
                    //尝试拉起该服务 进程间通信
                    PlayerMusicService.startWork(getApplicationContext());
                } else {
                    Log.e("LiveJobService", "服务ing" + count);
                }
                handler.sendEmptyMessageDelayed(100, 2000);
            }
        }
    };
    private long count;


    @Override
    public void onDestroy() {
        super.onDestroy();
        UtilsKt.logE("LiveJobService", "进程被杀掉了");
    }

    @Override
    protected void onHandleWork(Intent intent) {
        //LiveJobService使用的是队列的数据结构
        //我们搞一个定时任务来拉活传过来的服务
        if (handler != null  ) {
            handler.sendEmptyMessageDelayed(100, 2000);
        }
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, LiveJobService.class, JOB_ID, work);
    }
}
