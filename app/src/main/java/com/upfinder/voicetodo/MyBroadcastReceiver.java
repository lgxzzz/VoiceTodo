package com.upfinder.voicetodo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.upfinder.voicetodo.service.PlayerMusicService;
import com.upfinder.voicetodo.utils.UtilsKt;

/**
 * @author: ywj
 * @date: 2019/2/27 11:46
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UtilsKt.logE("MyBroadcastReceiver-ACTION_SCREEN_ON");
        if (!PlayerMusicService.isAlive) {
            context.startService(new Intent(context, PlayerMusicService.class));
        }
    }
}
