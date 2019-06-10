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
import com.upfinder.voicetodo.view.EventsDialog
import java.util.*


class NotificationWorker : Worker() {

    private val appId = "15286882"
    private val apiKey = "ta9vfyCht48NV2HZX1Eo9Pnh"
    private val secretKey = "MSQCS8HAQLxEMxu76iCfepsLA0bYbKwW"
    private val ttsMode: TtsMode = TtsMode.ONLINE
    private lateinit var speechSynthesizer: SpeechSynthesizer


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
//            setTTS(index, task)
        }
        //
        if(task.events.length>0){
            MyApplication.showEventsDialog(task)
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

    private fun setTTS(index: Int, task: Task) = Thread {
        kotlin.run {
            var speakContent = ""

            when (index) {
                1 -> {
                    when (task.alarmType) {
                        AddTaskActivity.ALARM_REPEAT -> speakContent += "固定事件"
                        AddTaskActivity.ALARM_SINGLE -> speakContent += "临时事件"
                        AddTaskActivity.ALARM_EVENTS -> speakContent += "事件包"
                    }
                    speakContent += "提醒您，距离您" + task.getFormatTitle() + "还有30分钟"
                }
                2 -> {
                    speakContent += "距离您" + task.getFormatTitle() + "还有5分钟"
                }
                3 -> {
                    speakContent += "您" + task.getFormatTitle() + "的时间到了"
                }
            }
            initTTS()
            try {
//
////                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
////                    val r = RingtoneManager.getRingtone(MyApplication.getInstance(), notification)
////                    r.play()
////                    SystemClock.sleep(4000)
////                    r.stop()
//
////                val mp3Url = "file:///android_asset/alerm.mp3"
//                val mp3 = MyApplication.getInstance().assets.openFd("alerm.mp3")
//                val mMediaPlayer = MediaPlayer()
//                mMediaPlayer.reset()
////                mMediaPlayer.setDataSource(mp3Url)
//                mMediaPlayer.setDataSource(mp3.fileDescriptor)
//                mMediaPlayer.prepare()
//                mMediaPlayer.isLooping = false
//                mMediaPlayer.setOnPreparedListener { mp -> mp.start() }
//                mMediaPlayer.setOnCompletionListener { mp -> playTTS(speakContent) }


                val notificationUrl = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationUrl)
                ringtone.play()
                val vibrator = MyApplication.getInstance().getSystemService(VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(500)
                SystemClock.sleep(5000)
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
                playTTS(speakContent)

            } catch (e: Exception) {
                e.printStackTrace()
                playTTS(speakContent)
            } finally {

            }


        }
    }.start()

//        playTTS("待做事件提醒：" + task.getHourMinute() + "," + task.getFormatTitle())
//        playTTS("您" + task.getFormatTitle()+ "的时间到了" )

    private fun playTTS(text: String) {
        if (speechSynthesizer != null) {
            val result: Int = speechSynthesizer.speak(text)
            checkResult(result, "speak")
        }
    }

    private fun initTTS() {
        val listener: SpeechSynthesizerListener = MessageListener()

        speechSynthesizer = SpeechSynthesizer.getInstance()
        speechSynthesizer.setContext(applicationContext)

        speechSynthesizer.setSpeechSynthesizerListener(listener)

        // 3. 设置appId，appKey.secretKey
        var result = speechSynthesizer.setAppId(appId)
        checkResult(result, "setAppId")
        result = speechSynthesizer.setApiKey(apiKey, secretKey)
        checkResult(result, "setApiKey")

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0")
        // 设置合成的音量，0-9 ，默认 5
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9")
        // 设置合成的语速，0-9 ，默认 5
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5")
        // 设置合成的语调，0-9 ，默认 5
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5")

        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT)
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        speechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL)

        // 6. 初始化
        result = speechSynthesizer.initTts(ttsMode)
        checkResult(result, "initTts")
    }


    private fun checkResult(result: Int, method: String) {
        if (result != 0) {
            logE("error code :$result method:$method 错误码文档:http://yuyin.baidu.com/docs/tts/122 ")
        }
    }


}