package com.upfinder.voicetodo.data.entitys

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.text.TextUtils
import com.upfinder.voicetodo.task.AddTaskActivity
import com.upfinder.voicetodo.utils.intformat2
import java.io.Serializable
import java.util.*


@Entity(tableName = "tasks")
data class Task constructor(
    var title: String,//标题
    var description: String,//描述
    var alarmType: Int = AddTaskActivity.ALARM_REPEAT,//闹钟类型
    var repeatPeriod: Int = 1,//重复周期
    var calendar: Calendar,//时间
    var color: Int,//颜色
    var notifiType: Int= AddTaskActivity.NOTIFITYPE_BELL,//通知类型
    var events : String,//事件包
    @PrimaryKey @ColumnInfo(name = "id") val id: String = (System.currentTimeMillis()/1000).toInt().toString()
//    @PrimaryKey @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString()
) : Serializable {


    @ColumnInfo(name = "hasReminder")
    var hasReminder = false  //是否已经提醒过了

    @ColumnInfo(name = "isRFIN")
    var isRepeatFirstInitNotifi = true  //是否是重复事件的第一次提醒

    val isEmpty
        get() = title.isEmpty() && description.isEmpty()

    fun getFormatTitle(): String {
        return if (!TextUtils.isEmpty(title)) title.trim() else description.trim()
    }

    fun getHourMinute(): String {
        return intformat2(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + intformat2(calendar.get(Calendar.MINUTE))
    }
}

