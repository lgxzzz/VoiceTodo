package com.upfinder.voicetodo.service

import android.app.IntentService
import android.content.Intent
import android.speech.tts.TextToSpeech
import com.upfinder.voicetodo.utils.logE

class AlarmService : IntentService("alarm-service-voice") {

    override fun onHandleIntent(p0: Intent?) {

        logE("服务启动", "成功接受到服务")
        //这里机型声音播放之类相关操作


        val textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { })

//        val result = textToSpeech.setLanguage(Locale.ENGLISH)
//        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Log.e("语音播报", "数据丢失或不支持")
//        }

        if (!textToSpeech.isSpeaking) {
            textToSpeech.speak("Successful voice broadcast, a lot of content", TextToSpeech.QUEUE_FLUSH, null)
        }

    }
}