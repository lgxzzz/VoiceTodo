package com.upfinder.voicetodo.worker

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.SystemClock
import android.os.Vibrator
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.listener.MessageListener
import com.upfinder.voicetodo.task.AddTaskActivity
import com.upfinder.voicetodo.utils.logE

class BaiduTTsApi constructor(){

    private val appId = "15286882"
    private val apiKey = "ta9vfyCht48NV2HZX1Eo9Pnh"
    private val secretKey = "MSQCS8HAQLxEMxu76iCfepsLA0bYbKwW"
    private val ttsMode: TtsMode = TtsMode.ONLINE
    private lateinit var speechSynthesizer: SpeechSynthesizer
    private var mContext : Context ?= null
    companion object {
        private var instance: BaiduTTsApi? = null
            get() {
                if (field == null) {
                    field = BaiduTTsApi()
                }
                return field
            }

        @Synchronized
        fun get(): BaiduTTsApi{
            return instance!!
        }
    }

    fun setTTS(index: Int, task: Task) = Thread {
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
            try {

                val notificationUrl = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(mContext, notificationUrl)
                ringtone.play()
                val vibrator = MyApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(500)
                SystemClock.sleep(5000)
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
                playTTS(speakContent)
                //
                if(task.events.length>0){
                    MyApplication.showEventsDialog(task)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                playTTS(speakContent)
            } finally {

            }


        }
    }.start()

//        playTTS("待做事件提醒：" + task.getHourMinute() + "," + task.getFormatTitle())
//        playTTS("您" + task.getFormatTitle()+ "的时间到了" )

    fun playTTS(text: String) {
        if (speechSynthesizer != null) {
            val result: Int = speechSynthesizer.speak(text)
            checkResult(result, "speak")
        }
    }

    fun initTTS(context: Context) {
        mContext = context;
        val listener: SpeechSynthesizerListener = MessageListener()

        speechSynthesizer = SpeechSynthesizer.getInstance()
        speechSynthesizer.setContext(context)

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