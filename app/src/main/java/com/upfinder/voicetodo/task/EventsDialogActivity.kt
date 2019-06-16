package com.upfinder.voicetodo.task

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.worker.BaiduTTsApi
import kotlinx.android.synthetic.main.events_dialog.*
import org.json.JSONArray
import org.json.JSONObject

class EventsDialogActivity : Activity(){

    private var mEventsArrary: JSONArray? = null
    private var mCurrentEvent: JSONObject? = null
    private var mIndex = 0
    private var isFinish = false
    private var mTaskId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.events_dialog)
        val events = intent.getStringExtra("events")
        mTaskId = intent.getStringExtra("taskId")
        setEvents(events)
        initView()
    }

    fun setEvents(events: String) {
        try {
            mEventsArrary = JSONArray(events)
            mIndex = 0
            refreshUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun initView() {

        rgEventState.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, i ->
            try {
                when (i) {
                    R.id.rbSuccess -> mCurrentEvent!!.put("state", AddTaskActivity.EVENTS_STATE_SUCCESS)
                    R.id.rbFail -> mCurrentEvent!!.put("state", AddTaskActivity.EVENTS_STATE_FAIL)
                    R.id.rbIgnore -> mCurrentEvent!!.put("state", AddTaskActivity.EVENTS_STATE_IGNORE)
                }
                mEventsArrary!!.put(mIndex, mCurrentEvent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        btPreEvent.setOnClickListener(View.OnClickListener {
            if (isFinish) {
                saveEvent()
            } else {
                mIndex--
                refreshUI()
            }
        })

        btNextEvent.setOnClickListener(View.OnClickListener {
            if (isFinish) {
                clearEvent()
            } else {
                mIndex++
                refreshUI()
            }
        })

        eventCancel.setOnClickListener(View.OnClickListener {
            finish();
        })
    }

    //刷新界面
    fun refreshUI() {
        mHandler.removeCallbacksAndMessages(null)
        mHandler.sendEmptyMessage(HANDLER_MSG_REFRESHUI)
    }

    //保存事件
    fun saveEvent() {
        finish()
    };

    //清除事件
    fun clearEvent() {
        MyApplication.getTasksLocalDataSourceInstance().deleteTask(mTaskId)
        finish()
    }

    val HANDLER_MSG_REFRESHUI = 1

    var mHandler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            HANDLER_MSG_REFRESHUI -> try {
                mCurrentEvent = mEventsArrary!!.getJSONObject(mIndex)
                val event = mCurrentEvent!!.getString("event")
                val state = mCurrentEvent!!.getInt("state")
                eventTitle.setText(event)
                when (state) {
                    AddTaskActivity.EVENTS_STATE_DEFAULT, AddTaskActivity.EVENTS_STATE_SUCCESS -> {
                        rbSuccess.setChecked(true)
                        rbIgnore.setChecked(false)
                        rbFail.setChecked(false)
                    }
                    AddTaskActivity.EVENTS_STATE_FAIL -> {
                        rbSuccess.setChecked(false)
                        rbIgnore.setChecked(false)
                        rbFail.setChecked(true)
                    }
                    AddTaskActivity.EVENTS_STATE_IGNORE -> {
                        rbSuccess.setChecked(false)
                        rbIgnore.setChecked(true)
                        rbFail.setChecked(false)
                    }
                }
                MyApplication.getInstance()
                //长度相等时，显示“保存”和“清除”
                if (mIndex + 1 == mEventsArrary!!.length()) {
                    isFinish = true
                    btPreEvent.setEnabled(true)
                    btPreEvent.setText("保存")
                    btNextEvent.setText("清除")
                } else if (mIndex == 0) {
                    isFinish = false
                    btPreEvent.setEnabled(false)
                    btPreEvent.setText("上一项")
                    btNextEvent.setText("下一项")
                } else {
                    isFinish = false
                    btPreEvent.setEnabled(true)
                    btNextEvent.setEnabled(true)
                    btPreEvent.setText("上一项")
                    btNextEvent.setText("下一项")
                }
                MyApplication.getTTs().playTTS(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        false
    })
}