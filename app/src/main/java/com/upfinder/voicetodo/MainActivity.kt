package com.upfinder.voicetodo

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.CalendarContract
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.upfinder.voicetodo.activity.MianzeActivity
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.data.entitys.TaskEvent
import com.upfinder.voicetodo.service.PlayerMusicService
import com.upfinder.voicetodo.task.AddTaskActivity
import com.upfinder.voicetodo.task.HistoryActivity
import com.upfinder.voicetodo.task.TaskAdapter
import com.upfinder.voicetodo.task.TaskEventAdapter
import com.upfinder.voicetodo.utils.logE
import com.upfinder.voicetodo.worker.LiveJobService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val PERMISSION_REQUESTCODE = 123
    private lateinit var singleAdapter: TaskAdapter
    private lateinit var repeatAdapter: TaskAdapter
    private lateinit var eventsAdapter: TaskEventAdapter
    private lateinit var screenOnReceiver:MyBroadcastReceiver
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            logE("CalendarObserver2" + msg.obj.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        initPermission()

        //注册日程事件监听
//        val obser = CalendarObserver(this@MainActivity, handler)
//        contentResolver.registerContentObserver(CalendarContract.Events.CONTENT_URI, true, obser);


        screenOnReceiver = MyBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filter)

    }

    private fun initView() {

        setSupportActionBar(toolbar)
//        supportActionBar?.setHomeButtonEnabled(true)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = resources.getString(R.string.app_name)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


        fab_repeat.setOnClickListener {
            AddTaskActivity.launch(this@MainActivity, AddTaskActivity.ALARM_EVENTS)
        }

        fab_single.setOnClickListener {
            AddTaskActivity.launch(this@MainActivity, AddTaskActivity.ALARM_SINGLE)
            //启动服务
            //            val intent = Intent(this, AlarmService::class.java)
            //            startService(intent)
        }
    }

    private fun initData() {
        initAdapter()

    }

    private fun initAdapter() {
        repeatAdapter = TaskAdapter(this, arrayListOf())
        repeatAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        repeatAdapter.setOnManagerListener(object : TaskAdapter.OnTaskManageListener {
            override fun onDel(task: Task) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("确定删除这条记录吗？")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        MyApplication.getTasksLocalDataSourceInstance().deleteTask(task.id)
                        refreshTasks()
                    }.create().show()

            }

            override fun onEdit(task: Task) {
                AddTaskActivity.launch(this@MainActivity, task)
            }

            override fun onCancel(task: Task) {
            }

        })
        eventsAdapter = TaskEventAdapter(this, arrayListOf())
        eventsAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        eventsAdapter.setOnManagerListener(object : TaskEventAdapter.OnTaskManageListener {
            override fun onDel(task: Task) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("确定删除这条记录吗？")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        MyApplication.getTasksLocalDataSourceInstance().deleteTask(task.id)
                        refreshTasks()
                    }.create().show()

            }

            override fun onEdit(task: Task) {
                AddTaskActivity.launch(this@MainActivity, task)
            }

            override fun onCancel(task: Task) {
            }

        })
        singleAdapter = TaskAdapter(this, arrayListOf())
        singleAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        singleAdapter.setOnManagerListener(object : TaskAdapter.OnTaskManageListener {
            override fun onDel(task: Task) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("确定删除这条记录吗？")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        MyApplication.getTasksLocalDataSourceInstance().deleteTask(task.id)
                        refreshTasks()
                    }.create().show()

            }

            override fun onEdit(task: Task) {
                AddTaskActivity.launch(this@MainActivity, task)
            }

            override fun onCancel(task: Task) {
            }

        })
        rvTaskRepeat.layoutManager = LinearLayoutManager(this)
        rvTaskRepeat.adapter = eventsAdapter
        rvTaskSingle.layoutManager = LinearLayoutManager(this)
        rvTaskSingle.adapter = singleAdapter

    }


    override fun onResume() {
        super.onResume()
        refreshTasks()
//        SettingUtils.enterWhiteListSetting(this@MainActivity)
//defaultMediaPlayer(this@MainActivity)
//defaultAlarmMediaPlayer(this@MainActivity)
//defaultCallMediaPlayer(this@MainActivity)
//        val queryEvents = CalendarUtils.queryEvents(this)
//        logE("")


    }

    private fun initWork() {
//        val result = CalendarUtils.insertCalendarEvent(
//            this@MainActivity,
//            "测试title",
//            "测试描述",
//            System.currentTimeMillis() + 62 * 1000,
//            System.currentTimeMillis() + 122 * 1000,
//            0XFFFF0000,
//            5*1000
//        )
//        if (!result) {
//            toast(this@MainActivity, "添加时间失败")
//        }
        PlayerMusicService.startWork(this)

        val workIntent = Intent()
        workIntent.putExtra("work", "xxxx");
        LiveJobService.enqueueWork(MyApplication.getInstance(), workIntent);
    }


    private fun refreshTasks() {
        MyApplication.getTasksLocalDataSourceInstance().getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                val repeatTasks: ArrayList<Task> = arrayListOf()
                val singleTasks: ArrayList<Task> = arrayListOf()
                val eventTasks: ArrayList<TaskEvent> = arrayListOf()
                tasks.forEach { task ->
                    when (task.alarmType) {
                        AddTaskActivity.ALARM_SINGLE -> {
                            if (task.calendar.timeInMillis >= Calendar.getInstance().timeInMillis) {
                                singleTasks.add(task)
                            }
                        }
                        AddTaskActivity.ALARM_REPEAT -> repeatTasks.add(task)
                        AddTaskActivity.ALARM_EVENTS -> {
                            try {
                                var events : String = task.events
                                var jsonArrary : JSONArray = JSONArray(events)
                                for (i in 0..jsonArrary.length()-1){
                                    var obj: JSONObject = jsonArrary.get(i) as JSONObject
                                    var event : String = obj.get("event") as String
                                    var state : Int = obj.get("state") as Int
                                    var taskEvent : TaskEvent = TaskEvent(task,i,event,state)
                                    eventTasks.add(taskEvent)
                                }
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                    }
                }

                if (repeatTasks.isEmpty()) {
                    addEmptyView(repeatTasks)

                }
                eventsAdapter.setNewData(eventTasks)
                repeatAdapter.setNewData(repeatTasks)
                singleAdapter.setNewData(singleTasks)
            }

            override fun onDataNotAvailable() {
//                val repeatTasks: ArrayList<Task> = arrayListOf()
//                addEmptyView(repeatTasks)
                val eventTasks : ArrayList<TaskEvent> = arrayListOf()
                addEventEmptyView(eventTasks)

                eventsAdapter.setNewData(eventTasks)
//                repeatAdapter.setNewData(repeatTasks)
                singleAdapter.setNewData(arrayListOf())
            }

        })
    }

    private fun addEventEmptyView(eventTasks: ArrayList<TaskEvent>) {
        var task:Task = Task(
            title = "示例",
            description = "",
            alarmType = AddTaskActivity.ALARM_EVENTS,
            repeatPeriod = 1,
            calendar = Calendar.getInstance(),
            color = resources.getColor(R.color.task_default),
            notifiType = AddTaskActivity.NOTIFITYPE_BELL,
            events = "",
            id = (-10000).toString()
        )
        var index:Int = -1

        eventTasks.add(
            TaskEvent(task,index,"",-1)
        )
    }

    private fun addEmptyView(repeatTasks: ArrayList<Task>) {
        repeatTasks.add(
            Task(
                title = "示例",
                description = "",
                alarmType = AddTaskActivity.ALARM_EVENTS,
                repeatPeriod = 1,
                calendar = Calendar.getInstance(),
                color = resources.getColor(R.color.task_default),
                notifiType = AddTaskActivity.NOTIFITYPE_BELL,
                events = "",
                id = (-10000).toString()
            )
        )
    }

    private var lastPressBackTime: Long = 0
    override fun onBackPressed() {
        moveTaskToBack(false)

//        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//            drawer_layout.closeDrawer(GravityCompat.START)
//        } else if (System.currentTimeMillis() - lastPressBackTime > 2000) {
//            lastPressBackTime = System.currentTimeMillis()
//            toast(this@MainActivity,"再按一次退出APP")
//        } else {
//            moveTaskToBack(false)
////            super.onBackPressed()
//        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_settings -> return true
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_history -> startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            R.id.nav_repeat ->  AddTaskActivity.launch(this@MainActivity, AddTaskActivity.ALARM_REPEAT)

            R.id.nav_share -> {
            }
            R.id.nav_user -> {
            }
            R.id.nav_about -> {
            }
            R.id.nav_mianze -> startActivity(Intent(this@MainActivity, MianzeActivity::class.java))
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    /**
     * android 6.0 以上需要动态申请权限
     */
    private fun initPermission() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.VIBRATE

        )

        val toApplyList = ArrayList<String>()

        for (perm in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm)

            }
        }

        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toTypedArray(), PERMISSION_REQUESTCODE)
        } else {
            initWork()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUESTCODE && grantResults.isNotEmpty()) {
            initWork()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (screenOnReceiver != null) {
            unregisterReceiver(screenOnReceiver)
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}