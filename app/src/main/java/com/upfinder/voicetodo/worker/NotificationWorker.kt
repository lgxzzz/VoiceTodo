package com.upfinder.voicetodo.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.Vibrator
import androidx.work.Worker
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.listener.MessageListener
import com.upfinder.voicetodo.task.AddTaskActivity
import com.upfinder.voicetodo.task.ReminderActivity
import com.upfinder.voicetodo.utils.intformat2
import com.upfinder.voicetodo.utils.logE
import java.util.*


class NotificationWorker : Worker() {


    override fun doWork(): Worker.WorkerResult {

        //id
        val taskId = inputData.getString("taskId", "")
        //第几次通知 1:前30分钟;2:前5分钟;3:前0分钟;
        val index = inputData.getInt("index", 0)

        logE("执行任务成功id:$taskId")
        val task =
            MyApplication.getTasksLocalDataSourceInstance().tasksDao.getTaskById(taskId) ?: return WorkerResult.FAILURE
        if (index == 0) return WorkerResult.FAILURE

        logE("执行任务成功name:" + task.getFormatTitle())

//        when (task.alarmType) {
//            AddTaskActivity.ALARM_SINGLE -> {
//
//            }
//            AddTaskActivity.ALARM_REPEAT -> {
//                //重复固定事件，第一次没有延迟，故添加标记 忽略
//                if (task.isRepeatFirstInitNotifi){
//                    task.isRepeatFirstInitNotifi=false
//                    MyApplication.getTasksLocalDataSourceInstance().tasksDao.updateTask(task)
//                    return WorkerResult.RETRY
//                }
//            }
//        }

        setNotification(index, task)

        if (task.notifiType == AddTaskActivity.NOTIFITYPE_BELL) {
            MyApplication.getTTs().setTTS(index,task);
        }

        return WorkerResult.SUCCESS
    }

    private fun setNotification(index: Int, task: Task) {
        val manager: NotificationManager =
            MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(MyApplication.getInstance(), ReminderActivity::class.java)
        intent.putExtra("taskId", task.id)
        intent.putExtra("index", index)
//        val delIntent = Intent(MyApplication.getInstance(), ReminderActivity::class.java)
//        delIntent.putExtra("cancel", "任务已取消")
//        delIntent.putExtra("taskId", "0")
//        delIntent.putExtra("index", index)

        val soundUrl: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: Notification.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("1", "默认渠道", NotificationManager.IMPORTANCE_DEFAULT);
            //        mChannel.description="描述"
            //        mChannel.enableLights(true)
            //        mChannel.enableVibration(true)
            manager.createNotificationChannel(mChannel);
            if (task.notifiType == AddTaskActivity.NOTIFITYPE_VIBRATE) {
                mChannel.enableVibration(true)
            }
            builder = Notification.Builder(MyApplication.getInstance(), mChannel.id)
        } else {
            builder = Notification.Builder(MyApplication.getInstance())
        }

        val timeSb: StringBuilder = StringBuilder()
        timeSb.append("今天 ")
        timeSb.append(intformat2(task.calendar.get(Calendar.HOUR_OF_DAY)))
        timeSb.append(":")
        timeSb.append(intformat2(task.calendar.get(Calendar.MINUTE)))

        builder.setContentTitle("事件提醒:" + task.getFormatTitle())
            .setContentText(timeSb.toString())
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            //            .setDefaults(Notification.DEFAULT_SOUND)

//            .setDeleteIntent(
//                PendingIntent.getActivity(
//                    MyApplication.getInstance(),
//                    1,
//                    delIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//                )
//            )
            .setContentIntent(
                PendingIntent.getActivity(
                    MyApplication.getInstance(),
                    2,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        if (task.notifiType == AddTaskActivity.NOTIFITYPE_VIBRATE) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
        }
        manager.notify(task.id.toInt(), builder.build())
    }



}