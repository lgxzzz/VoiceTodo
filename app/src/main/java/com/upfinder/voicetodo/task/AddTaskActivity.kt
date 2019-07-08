package com.upfinder.voicetodo.task

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.InputType
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.EditText
import android.widget.RadioButton
import cn.qqtheme.framework.picker.ColorPicker
import cn.qqtheme.framework.util.ConvertUtils
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.data.entitys.TaskEvent
import com.upfinder.voicetodo.utils.KeyboardUtils
import com.upfinder.voicetodo.utils.intformat2
import com.upfinder.voicetodo.utils.logE
import com.upfinder.voicetodo.utils.toast
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    var mLocalTask: Task? = null
    var mCalendar = Calendar.getInstance()
    /*闹钟类型*/
    var mAlarmType = ALARM_REPEAT
    var mNotifiType = NOTIFITYPE_BELL
    var mRepeatPeriod = 0
    var mEventChain: ArrayList<String> = arrayListOf()
    lateinit var mEvenAapter: EventAdapter

    private var mSelectedColor: Int = 0

    companion object {
        const val ALARM_REPEAT = 11
        const val ALARM_SINGLE = 12
        const val ALARM_EVENTS = 13
        const val NOTIFITYPE_BELL = 14
        const val NOTIFITYPE_VIBRATE = 15
        const val EVENTS_STATE_SUCCESS = 16
        const val EVENTS_STATE_IGNORE = 17
        const val EVENTS_STATE_FAIL = 18
        const val EVENTS_STATE_DEFAULT = 19
        //提醒周期
        var NOTIFI_REPEAT_PERIOD = intArrayOf(1, 2, 3, 5, 7, 10, 15, 0)

        val AM_ACTION = "com.upfinder.voiceTask.alarm.action"
        val PARAM_TASK = "param-task-taskid"

        fun launch(context: Context, alarmType: Int) {
            context.startActivity(Intent(context, AddTaskActivity::class.java).putExtra("alarmType", alarmType))
        }

        fun launch(context: Context, task: Task) {
            context.startActivity(Intent(context, AddTaskActivity::class.java).putExtra("task", task))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        mSelectedColor = resources.getColor(R.color.task_17)


        mAlarmType = intent.getIntExtra("alarmType", ALARM_REPEAT)
        mLocalTask = intent.getSerializableExtra("task") as Task?

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "添加提醒"
        NOTIFI_REPEAT_PERIOD[7] = 0
        mLocalTask?.let {
            tvTitle.text = "编辑提醒"
            mAlarmType = mLocalTask!!.alarmType
            mRepeatPeriod = mLocalTask!!.repeatPeriod
            etTodoName.setText(mLocalTask!!.title)
            etTodoName.setSelection(etTodoName.text.toString().length)
            etTodoDesc.setText(mLocalTask!!.description)
            mCalendar = mLocalTask!!.calendar
            mSelectedColor = mLocalTask!!.color

            tvChoiceDate.text = intformat2(mCalendar.get(Calendar.YEAR)) + "/" +
                    intformat2(mCalendar.get(Calendar.MONTH) + 1) + "/" +
                    intformat2(mCalendar.get(Calendar.DATE))
            tvChoiceTime.text = intformat2(mCalendar.get(Calendar.HOUR_OF_DAY)) + " : " +
                    intformat2(mCalendar.get(Calendar.MINUTE))

            if (it.repeatPeriod !in NOTIFI_REPEAT_PERIOD) {
                NOTIFI_REPEAT_PERIOD[7] = it.repeatPeriod
                rbRepeatPeriodZdy.text = it.repeatPeriod.toString() + "天"
                logE("设置了2 ${it.repeatPeriod}")
            }

            (rgRepeatPeriod.getChildAt(AddTaskActivity.NOTIFI_REPEAT_PERIOD.indexOf(it.repeatPeriod)) as RadioButton).isChecked = true
            tvRepeatPeriod.text = it.repeatPeriod.toString()

            if(it?.events!=null){
                mEventChain.clear();
                var events : String = it.events
                var jsonArrary : JSONArray = JSONArray(events)
                for (i in 0..jsonArrary.length()-1){
                    var obj: JSONObject = jsonArrary.get(i) as JSONObject
                    var event : String = obj.get("event") as String
                    var state : Int = obj.get("state") as Int
                    mEventChain.add(event);
                }
                mEvenAapter = EventAdapter(this@AddTaskActivity,mEventChain)
                mEvenAapter.setOnManagerListener(object : EventAdapter.OnEventManageListener{
                    override fun onDel(event: String, index: Int) {
                        mEventChain.removeAt(index);
                        mEvenAapter.notifyDataSetChanged()
                    }
                })
                lineEventsListview.visibility = View.VISIBLE;
                lineEventsListview.adapter = mEvenAapter;
                mEvenAapter.notifyDataSetChanged()
            }
        }

        ivChoiceColor.setColor(mSelectedColor)


        rgAlarmType.setOnCheckedChangeListener { radioGroup, i ->
            KeyboardUtils.hideSoftInput(this@AddTaskActivity)
            when (radioGroup.checkedRadioButtonId) {
                R.id.rbRepeat -> {
                    //若重复提醒 修改时间选择模式
                    mRepeatPeriod = 1
                    changeAlarmTypeLayout(ALARM_REPEAT)
                }
                R.id.rbSingle -> {
                    //单次提醒
                    mRepeatPeriod = 0
                    changeAlarmTypeLayout(ALARM_SINGLE)
                }
            }
        }
        rgRepeatPeriod.setOnCheckedChangeListener { group, checkedId ->
            KeyboardUtils.hideSoftInput(this@AddTaskActivity)

            if (checkedId == rbRepeatPeriodZdy.id) {


                val et = EditText(this@AddTaskActivity)
                et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                et.gravity = Gravity.CENTER
                val alertDialog = AlertDialog.Builder(this@AddTaskActivity)
                        .setTitle("请输入重复提醒时间")
                        .setView(et)
                        .setPositiveButton("确定") { dialog, _ ->

                            val content = et.text.toString()
                            rbRepeatPeriodZdy.text = content + "天"
                            NOTIFI_REPEAT_PERIOD[7] = content.toInt()
                            mRepeatPeriod = NOTIFI_REPEAT_PERIOD[7]

                            KeyboardUtils.hideSoftInput(this@AddTaskActivity)

                            dialog.dismiss()

                        }.create()
                alertDialog.setCancelable(false)
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
            } else {
                val index = group.indexOfChild(findViewById(checkedId))
                mRepeatPeriod = NOTIFI_REPEAT_PERIOD[index]
                tvRepeatPeriod.text = mRepeatPeriod.toString()
            }
        }

        rgNotifiType.setOnCheckedChangeListener { radioGroup, i ->
            KeyboardUtils.hideSoftInput(this@AddTaskActivity)
            when (radioGroup.checkedRadioButtonId) {
                R.id.rbBell -> {
                    mNotifiType = NOTIFITYPE_BELL
                    ivNotifiType.setImageDrawable(resources.getDrawable(R.drawable.ic_bell))
                }
                R.id.rbVibrate -> {
                    mNotifiType = NOTIFITYPE_VIBRATE
                    ivNotifiType.setImageDrawable(resources.getDrawable(R.drawable.ic_vibration))
                }
            }
        }


        tvChoiceDate.setOnClickListener {
            KeyboardUtils.hideSoftInput(this@AddTaskActivity)
            showDatePickerDialog(mCalendar)
        }

        tvChoiceTime.setOnClickListener {
            KeyboardUtils.hideSoftInput(this@AddTaskActivity)
            showTimePickerDialog(mCalendar)
        }

        eventAddBtn.setOnClickListener{
            val event = etEventName.text.toString();
            if (TextUtils.isEmpty(event)) {
                toast(this, "请输入事件内容")
            }else
            {

                etEventName.text.clear();
                mEventChain.add(event);

                mEvenAapter = EventAdapter(this@AddTaskActivity,mEventChain)
                mEvenAapter.setOnManagerListener(object : EventAdapter.OnEventManageListener{
                    override fun onDel(event: String, index: Int) {
                        mEventChain.removeAt(index);
                        mEvenAapter.notifyDataSetChanged()
                    }
                })
                lineEventsListview.visibility = View.VISIBLE;
                lineEventsListview.adapter = mEvenAapter;
                mEvenAapter.notifyDataSetChanged()
            }
        }

        chooseRingtone.setOnClickListener {
            toChooseRingtoneUri()
        }

        changeAlarmTypeLayout(mAlarmType)
        //临时事件隐藏周期选择
//        when (mAlarmType) {
//            ALARM_SINGLE -> {
//                llRepeationChoose.visibility = View.GONE
//                lineRepeationChoose.visibility = View.GONE
//            }
//        }

        changeNotifiTypeLayout(mNotifiType)
        var spanCount : Int = 2;
        if(isPad()){
            spanCount = 1;
        }

        rvColors.layoutManager = GridLayoutManager(this, spanCount, GridLayoutManager.HORIZONTAL, false)
        val adapter = TaskColorAdapter(this)
        adapter.setOnChoiceListener(object : TaskColorAdapter.OnChoiceListener {
            @SuppressLint("ResourceType")
            override fun onSelected(colorRes: Int) {

                mSelectedColor = colorRes
                ivChoiceColor.setBackgroundColor(mSelectedColor)
            }

        })
        rvColors.adapter = adapter
        ivChoiceColor.setOnClickListener {
            val picker = ColorPicker(this)
            picker.setInitColor(mSelectedColor)
            picker.setOnColorPickListener { pickedColor ->
                mSelectedColor = pickedColor
                logE(mSelectedColor.toString())
                ivChoiceColor.setBackgroundColor(
                        Color.parseColor(
                                "#" + ConvertUtils.toColorString(
                                        mSelectedColor,
                                        false
                                )
                        )
                )

            }
            picker.show()
        }

    }

    fun toChooseRingtoneUri(){
        var intent :Intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置通知铃声");
        startActivityForResult(intent, 2);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri : Uri ?= data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri !=null){
            MyApplication.saveChooseRingtoneUri(uri);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_task, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_done -> createTask()
            android.R.id.home -> finish()
        }
        return true
    }


    private fun createTask() {
        val title = etTodoName.text.toString()
        val content = etTodoDesc.text.toString()

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            toast(this, "请输入提醒内容")
            return
        }
        if (tvChoiceDate.text.contains("请选择")) {
            toast(this, "请选择提醒日期")
            return
        }
        if (tvChoiceTime.text.contains("请选择")) {
            toast(this, "请选择提醒时间")
            return
        }
        if (mRepeatPeriod == 0 && mCalendar.timeInMillis <= Calendar.getInstance().timeInMillis) {
            toast(this, "提醒时间不能小于当前时间")
            return
        }
        logE("mRepeatPeriod--$mRepeatPeriod")

        val eventsJSONArray = JSONArray()
        for (event in mEventChain){
            val eventObject = JSONObject();
            eventObject.put("event",event)
            eventObject.put("state",EVENTS_STATE_DEFAULT)
            eventsJSONArray.put(eventObject)
        }

        val events = eventsJSONArray.toString()

        val currentTask = Task(
                title = title,
                description = content,
                alarmType = mAlarmType,
                repeatPeriod = mRepeatPeriod,
                color = mSelectedColor,
                notifiType = mNotifiType,
                calendar = mCalendar,
                events = events
        )



        when (currentTask.alarmType) {
            AddTaskActivity.ALARM_SINGLE -> {
                //单次临时事件使用本地task任务
                MyApplication.notifiTask(mLocalTask, currentTask)
            }
            AddTaskActivity.ALARM_REPEAT -> {
                //固定重复事件使用日历任务
                MyApplication.notifiTask(mLocalTask, currentTask)
            }
            AddTaskActivity.ALARM_EVENTS -> {
                //固定重复事件使用日历任务
                MyApplication.notifiTask(mLocalTask, currentTask)
            }
        }



        onBackPressed()
    }

    private fun changeAlarmTypeLayout(alarmType: Int) {
        if(!this.mAlarmType.equals(ALARM_EVENTS)){
            this.mAlarmType = alarmType
        }
        when (alarmType) {
            ALARM_REPEAT -> {
//                tvChoiceDate.visibility = View.GONE
                llRepeationPeriod.visibility = View.VISIBLE
                lineRepeationPeriod.visibility = View.VISIBLE
//                viewTimeChooseDivider.visibility = View.GONE
                if (rgAlarmType.checkedRadioButtonId != rbRepeat.id) {
                    rgAlarmType.check(rbRepeat.id)
                }

            }

            ALARM_SINGLE -> {
//                tvChoiceDate.visibility = View.VISIBLE
                llRepeationPeriod.visibility = View.GONE
                lineRepeationPeriod.visibility = View.GONE
//                viewTimeChooseDivider.visibility = View.VISIBLE
                if (rgAlarmType.checkedRadioButtonId != rbSingle.id) {
                    rgAlarmType.check(rbSingle.id)
                }

            }

            ALARM_EVENTS -> {
//                tvChoiceDate.visibility = View.VISIBLE
                llRepeationPeriod.visibility = View.GONE
                lineRepeationPeriod.visibility = View.GONE
                lineEvents.visibility = View.VISIBLE
//                viewTimeChooseDivider.visibility = View.VISIBLE
                if (rgAlarmType.checkedRadioButtonId != rbSingle.id) {
                    rgAlarmType.check(rbSingle.id)
                }

            }

        }

    }


    private fun changeNotifiTypeLayout(notifiType: Int) {
        when (notifiType) {
            NOTIFITYPE_BELL -> {
                rbBell.isChecked = true
            }

            NOTIFITYPE_VIBRATE -> {
                rbVibrate.isChecked = true
            }
        }

    }

    //展示日期选择
    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog(calendar: Calendar) {
        val datePickerDialog = DatePickerDialog(
                this@AddTaskActivity,
                DatePickerDialog.OnDateSetListener { _, year, month, day ->

                    mCalendar.set(
                            year, month, day,
                            mCalendar.get(Calendar.HOUR_OF_DAY),
                            mCalendar.get(Calendar.MINUTE),
                            0
                    )
                    tvChoiceDate.text = intformat2(year) + "/" + intformat2(month + 1) + "/" + intformat2(day)
//                Toast.makeText(this@AddTaskActivity, "日期选择完成", Toast.LENGTH_SHORT).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()

    }


    //展示时间选择
    @SuppressLint("SetTextI18n")
    private fun showTimePickerDialog(calendar: Calendar) {
        val timePickerDialog = TimePickerDialog(
                this@AddTaskActivity,
                TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    mCalendar.set(
                            mCalendar.get(Calendar.YEAR),
                            mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                            hour,
                            minute,
                            0
                    )
                    tvChoiceTime.text = intformat2(hour) + " : " + intformat2(minute)
                    //                Toast.makeText(this@AddTaskActivity, "时间选择完成", Toast.LENGTH_SHORT).show()
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        )

        timePickerDialog.show()
    }

    /**
 * 判断是否为平板
 *
 * @return
 */
 fun  isPad() : Boolean{
	var wm : WindowManager = this@AddTaskActivity.getSystemService(Context.WINDOW_SERVICE) as WindowManager;
	var  display :Display = wm.getDefaultDisplay();
	// 屏幕宽度
	var screenWidth : Int = display.getWidth()
        ;
	// 屏幕高度
	var  screenHeight : Int = display.getHeight()
	var  dm : DisplayMetrics = DisplayMetrics();
	display.getMetrics(dm);
	var x : Double = Math.pow((dm.widthPixels / dm.xdpi).toDouble(), 2.0);
	var y : Double = Math.pow((dm.heightPixels / dm.ydpi).toDouble(), 2.0);
	// 屏幕尺寸
	var screenInches : Double = Math.sqrt(x + y);
	// 大于6尺寸则为Pad
	if (screenInches >= 6.0) {

		return true;
	}
	return false;
}


}