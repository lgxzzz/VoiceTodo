package com.upfinder.voicetodo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.upfinder.voicetodo.utils.logE
import com.upfinder.voicetodo.worker.NotificationWorker

class MyAlarmBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent) {

        logE("接收到闹钟广播")

        val taskId = intent.getStringExtra("taskId")
        val index = intent.getIntExtra("index", 0)


        val data = Data.Builder().putString("taskId", taskId).putInt("index", index).build()
        val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .addTag(taskId)
            .setInputData(data)
            .build()

        WorkManager.getInstance().enqueue(request)
    }

}