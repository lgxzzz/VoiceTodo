package com.upfinder.voicetodo.task

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.upfinder.voicetodo.MyApplication
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.R.id.rvHistory
import com.upfinder.voicetodo.data.entitys.Task
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "临时事件提醒记录"
        initData()
        initView()
    }

    private fun initData() {
        historyAdapter = TaskAdapter(this, arrayListOf())
        historyAdapter.setOnManagerListener(object : TaskAdapter.OnTaskManageListener {
            override fun onDel(task: Task) {
                AlertDialog.Builder(this@HistoryActivity)
                    .setTitle("确定删除这条记录吗？")
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        MyApplication.getTasksLocalDataSourceInstance().deleteTask(task.id)
                        refreshTasks()
                    }.create().show()

            }

            override fun onEdit(task: Task) {
                AddTaskActivity.launch(this@HistoryActivity, task)
            }

            override fun onCancel(task: Task) {
            }

        })

    }

    private fun initView() {

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

    }

    override fun onResume() {
        super.onResume()
        refreshTasks()
    }

    private fun refreshTasks() {
        MyApplication.getTasksLocalDataSourceInstance().getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                val singleTasks: ArrayList<Task> = arrayListOf()
                tasks.forEach { task ->
                    when (task.alarmType) {
                        AddTaskActivity.ALARM_SINGLE -> {
                            if (task.calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                                singleTasks.add(task)
                            }
                        }
                        AddTaskActivity.ALARM_EVENTS-> {
                            if (task.calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                                singleTasks.add(task)
                            }
                        }
                    }
                }

                historyAdapter.setNewData(singleTasks)
            }

            override fun onDataNotAvailable() {
                historyAdapter.setNewData(arrayListOf())
            }

        })
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}
