package com.upfinder.voicetodo;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import com.upfinder.voicetodo.util.Utils;
import com.upfinder.voicetodo.utils.UtilsKt;

public class CalendarObserver extends ContentObserver {

    private Context context;
    private Handler handler;

    public CalendarObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        UtilsKt.logE("CalendarObserver events have chage");
        handler.obtainMessage(3, "CalendarObserver events have chage").sendToTarget();
    }


}