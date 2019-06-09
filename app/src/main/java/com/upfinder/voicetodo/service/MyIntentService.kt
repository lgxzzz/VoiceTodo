package com.upfinder.voicetodo.service

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.upfinder.voicetodo.utils.logE

class MyIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent?) {
         logE("服务启动", "成功接受到服务")
    }





}
