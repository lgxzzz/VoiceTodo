package com.upfinder.voicetodo

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import androidx.work.WorkManager
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors
import com.upfinder.voicetodo.data.AppDatabase
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.receiver.MyAlarmBroadcastReceiver
import com.upfinder.voicetodo.task.AddTaskActivity
import com.upfinder.voicetodo.task.EventsDialogActivity
import com.upfinder.voicetodo.task.ReminderActivity
import com.upfinder.voicetodo.util.CalendarUtils
import com.upfinder.voicetodo.utils.logE
import com.upfinder.voicetodo.utils.toast
import com.upfinder.voicetodo.worker.BaiduTTsApi
import java.util.*

class MyApplication : Application() {

    companion object {
          lateinit var  notification:Notification
        private lateinit var localDataSource: TasksLocalDataSource
        private lateinit var mApplication: MyApplication
        private lateinit var mTTsApi: BaiduTTsApi
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        fun getInstance(): Application {
            return mApplication
        }

        //显示程序事件弹窗
        fun showEventsDialog(task:Task){
            val intent = Intent(mApplication.baseContext, EventsDialogActivity::class.java)
            intent.putExtra("events", task.events)
            intent.putExtra("taskId", task.id)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            mApplication.startActivity(intent)
        }

        /*获取数据库实例*/
        fun getTasksLocalDataSourceInstance(): TasksLocalDataSource {
            return localDataSource
        }

        fun getTTs() : BaiduTTsApi{
            return mTTsApi
        }
        /**
         * 创建task任务
         */
        fun notifiTask(lastTask: Task?, currentTask: Task) {

            //取消上一个任务
            lastTask?.let {
                MyApplication.getTasksLocalDataSourceInstance().deleteTask(lastTask.id)
                WorkManager.getInstance().cancelAllWorkByTag(lastTask.id)
                val alarmManager: AlarmManager =
                    mApplication.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(mApplication.getPendingIntent(lastTask.id, 0))
            }
            //添加新任务
            MyApplication.getTasksLocalDataSourceInstance().saveTask(currentTask)

            when (currentTask.alarmType) {
                AddTaskActivity.ALARM_SINGLE -> {
//                    mApplication.addSigleTask(currentTask)
                    mApplication.createSingleTask(currentTask)
                }
                AddTaskActivity.ALARM_REPEAT -> {
//                    mApplication.addRepeatTask(currentTask)
                    mApplication.createRepeatTask(currentTask)
                }
                AddTaskActivity.ALARM_EVENTS-> {
//                    mApplication.addRepeatTask(currentTask)
                    mApplication.createRepeatTask(currentTask)
                }
            }
            toast(mApplication, "创建提醒完成")

        }

        /**
         * 创建日历任务
         */
        fun notifiCalendarTask(lastTask: Task?, currentTask: Task) {

            //取消上一个任务
            lastTask?.let {
                MyApplication.getTasksLocalDataSourceInstance().deleteTask(lastTask.id)
            }
            //添加新任务
            MyApplication.getTasksLocalDataSourceInstance().saveTask(currentTask)



            val insertCalendarEvent = CalendarUtils.insertCalendarEvent(
                mApplication,
                currentTask.title,
                currentTask.description,
                currentTask.calendar.timeInMillis,
                currentTask.calendar.timeInMillis,
                currentTask.repeatPeriod,
                5 ,
                currentTask.color.toLong()
            )
            val insertResult = if (insertCalendarEvent) {
                "创建提醒完成"
            } else {
                "创建提醒失败"
            }
            toast(mApplication, insertResult)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApplication = this
        localDataSource = TasksLocalDataSource.getInstance(AppExecutors(), AppDatabase.getInstance(this).tasksDao())
        mTTsApi = BaiduTTsApi();
        mTTsApi.initTTS(this)
        setForeverNotification()

    }


    private fun setForeverNotification() {
        logE("setForeverNotification:"+Thread.currentThread().id)
        val manager: NotificationManager =
            MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(MyApplication.getInstance(), MainActivity::class.java)
        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("2", "默认渠道", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(mChannel)
            builder = NotificationCompat.Builder(MyApplication.getInstance(), mChannel.id)
        } else {
            builder = NotificationCompat.Builder(MyApplication.getInstance())
        }


        builder
//            .setContentTitle("事件提醒器")
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    MyApplication.getInstance(),
                    2,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
         notification = builder.build()

//        manager.notify(Random().nextInt(10000), notification)
    }


    //创建单次闹铃提醒
    private fun createSingleTask(task: Task) {

        val targetMillis = task.calendar.timeInMillis
        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(Calendar.SECOND, 0)
        val delayTimeMillis = targetMillis - currentCalendar.timeInMillis
        //每个任务提醒三次 前30分， 前5分，前0分钟

//        if (delayTimeMillis - 30 * 60 * 1000L > 0) {
//            logE("createSingleTask 1")
//            sendSingleAlarmManager(task.id, 1, targetMillis - 30 * 60 * 1000L)
//        }
        if (delayTimeMillis - 5 * 60 * 1000L > 0) {
            logE("createSingleTask 2")
            sendSingleAlarmManager(task.id, 2, targetMillis - 5 * 60 * 1000L)
        }
        if (delayTimeMillis > 0) {
            logE("createSingleTask 3")
            sendSingleAlarmManager(task.id, 3, targetMillis)
        }

    }

    private fun sendSingleAlarmManager(taskId: String, index: Int, targetMillis: Long) {
        logE("targetMillis $targetMillis")
        val pendingBroad = getPendingIntent(taskId, index)
        val alarmManager: AlarmManager = getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMillis, pendingBroad)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
//                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, targetMillis, pendingBroad)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetMillis, pendingBroad)
            }
            else -> {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetMillis, pendingBroad)
            }
        }
    }

    private fun getPendingIntent(taskId: String, index: Int): PendingIntent? {
        val intentBroad = Intent(AddTaskActivity.AM_ACTION)
        intentBroad.setPackage(packageName)
        intentBroad.setClass(this, MyAlarmBroadcastReceiver::class.java)
        intentBroad.putExtra("taskId", taskId)
        intentBroad.putExtra("index", index)
        intentBroad.action = taskId + index
        return PendingIntent.getBroadcast(this, taskId.toInt(), intentBroad, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    //创建重复闹铃提醒
    private fun createRepeatTask(task: Task) {

        val targetMillis = task.calendar.timeInMillis

        val currentCalendar = Calendar.getInstance()
        currentCalendar.set(Calendar.SECOND, 0)
        val delayTimeMillis = targetMillis - currentCalendar.timeInMillis


//        if (delayTimeMillis - 30 * 60 * 1000L > 0) {
//            sendRepeatManager(task, 1, targetMillis - 30 * 60 * 1000L)
//        } else {
//            sendRepeatManager(task, 1, targetMillis - 30 * 60 * 1000L + DAY_MILLIS)
//        }
        if (delayTimeMillis - 5 * 60 * 1000L > 0) {
            sendRepeatManager(task, 2, targetMillis - 5 * 60 * 1000L)
        } else {
            sendRepeatManager(task, 2, targetMillis - 5 * 60 * 1000L + DAY_MILLIS)
        }
        if (delayTimeMillis > 0) {
            sendRepeatManager(task, 3, targetMillis)
        } else {
            sendRepeatManager(task, 3, targetMillis + DAY_MILLIS)
        }

    }

    private fun sendRepeatManager(task: Task, index: Int, targetMillis: Long) {
        val pendingBroad = getPendingIntent(task.id, index)
        val alarmManager: AlarmManager = getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        val delay = DAY_MILLIS * task.repeatPeriod
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetMillis, delay, pendingBroad)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
//                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, targetMillis,DISTANCE_MILLIS*task.repeatPeriod, pendingBroad)
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetMillis, delay, pendingBroad)
            }
            else -> {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetMillis, delay, pendingBroad)
            }
        }
    }


}