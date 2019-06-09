package com.upfinder.voicetodo.task

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.utils.intformat2
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var taskId: String
    private var task: Task? = null
    private var index: Int = 0
    private var canEdit: Boolean = false


    companion object {
        fun launch(context: Context, taskId: String, index: Int, canEdit: Boolean) {
            val intent = Intent(context, ReminderActivity::class.java)
            intent.putExtra("canEdit", canEdit)
            intent.putExtra("taskId", taskId)
            intent.putExtra("index", index)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "提醒详情"

        canEdit = intent.getBooleanExtra("canEdit", false)
        taskId = intent.getStringExtra("taskId")
        index = intent.getIntExtra("index", -1)
        initView()
        initData()
    }

    private fun initView() {


        val radioButtons = arrayListOf<RadioButton>(
            rbRepeat,
            rbSingle,
            rbBell,
            rbVibrate,
            rbRepeatPeriod1,
            rbRepeatPeriod2,
            rbRepeatPeriod3,
            rbRepeatPeriod5,
            rbRepeatPeriod7,
            rbRepeatPeriod10,
            rbRepeatPeriod15
        )
        for (radioButton in radioButtons) {
            clearUsable(radioButton)
        }
        etTodoName.isClickable = false
        etTodoName.isFocusable = false
        etTodoDesc.isClickable = false
        etTodoDesc.isFocusable = false
        tvChoiceDate.isClickable = false
        tvChoiceTime.isClickable = false
        llColorChoose.visibility =  View.GONE
    }

    private fun clearUsable(view: RadioButton) {
        view.isFocusable = false
        view.isClickable = false
    }

    private fun initData() {
        Thread {
            kotlin.run {
                task = MyApplication.getTasksLocalDataSourceInstance().tasksDao.getTaskById(taskId)
                inflateData(task)
            }

        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun inflateData(task: Task?) {
        runOnUiThread {

            task?.let {
                etTodoName.setText(it.title)
                ivChoiceColor.setColor(it.color)
                etTodoDesc.setText(it.description)
                when (it.alarmType) {
                    AddTaskActivity.ALARM_REPEAT -> {
                        rbRepeat.isChecked = true
//                        tvChoiceDate.visibility = View.GONE
                        viewTimeChooseDivider.visibility = View.GONE
                    }
                    AddTaskActivity.ALARM_SINGLE -> {
                        rbSingle.isChecked = true
//                        tvChoiceDate.visibility = View.VISIBLE
                        viewTimeChooseDivider.visibility = View.VISIBLE
                    }
                }
                when (it.notifiType) {
                    AddTaskActivity.NOTIFITYPE_BELL -> rbBell.isChecked = true
                    AddTaskActivity.NOTIFITYPE_VIBRATE -> rbVibrate.isChecked = true
                }
                tvChoiceDate.text = intformat2(it.calendar.get(Calendar.YEAR)) + "/" +
                        intformat2(it.calendar.get(Calendar.MONTH) + 1) + "/" +
                        intformat2(it.calendar.get(Calendar.DATE))
                tvChoiceTime.text = intformat2(it.calendar.get(Calendar.HOUR_OF_DAY)) + " : " +
                        intformat2(it.calendar.get(Calendar.MINUTE))
                //临时事件隐藏周期选择
                when (task?.alarmType) {
                    AddTaskActivity.ALARM_SINGLE -> {
                        llRepeationChoose.visibility = View.GONE
                        lineRepeationChoose.visibility = View.GONE

                        llRepeationPeriod.visibility = View.GONE
                        lineRepeationPeriod.visibility = View.GONE
                    }
                }

                AddTaskActivity.NOTIFI_REPEAT_PERIOD[7] = 0
                if (it.repeatPeriod !in AddTaskActivity.NOTIFI_REPEAT_PERIOD) {
                    AddTaskActivity.NOTIFI_REPEAT_PERIOD[7] = it.repeatPeriod
                    rbRepeatPeriodZdy.text = it.repeatPeriod.toString() + "天"
                }
                (rgRepeatPeriod.getChildAt(AddTaskActivity.NOTIFI_REPEAT_PERIOD.indexOf(it.repeatPeriod)) as RadioButton).isChecked =
                        true
                tvRepeatPeriod.text = it.repeatPeriod.toString()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!canEdit) {
            menuInflater.inflate(R.menu.reminder_done, menu)
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_edit -> {
                finish()
                task?.let { AddTaskActivity.launch(this@ReminderActivity, it) }
            }
            R.id.action_know -> {
                finish()
            }
            android.R.id.home -> finish()
        }
        return true
    }
}
