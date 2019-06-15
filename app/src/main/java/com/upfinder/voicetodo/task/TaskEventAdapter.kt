package com.upfinder.voicetodo.task

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import cn.qqtheme.framework.util.ConvertUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.data.entitys.Task
import com.upfinder.voicetodo.data.entitys.TaskEvent
import com.upfinder.voicetodo.utils.toast

class TaskEventAdapter(var context: Context, data: List<TaskEvent>?) :
    BaseQuickAdapter<TaskEvent, BaseViewHolder>(R.layout.item_task_layout, data) {

    private lateinit var onTaskManageListener: TaskEventAdapter.OnTaskManageListener

    fun setOnManagerListener(onTaskManageListener: TaskEventAdapter.OnTaskManageListener) {
        this.onTaskManageListener = onTaskManageListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(holder: BaseViewHolder, taskEvent: TaskEvent) {
        val position = data.indexOf(taskEvent) + 1
        val task : Task = taskEvent.task
        val event : String = taskEvent.event;
        holder.getView<TextView>(R.id.tvTitle).text = "事件 $position:" + event
//        holder.tvContent.text = task.description
        holder.getView<TextView>(R.id.tvTime).text = task.getHourMinute()
        holder.getView<SwipeLayout>(R.id.swipeLayout).showMode = SwipeLayout.ShowMode.PullOut

        holder.getView<TextView>(R.id.tvDel).setOnClickListener {
            onTaskManageListener?.onDel(task)
        }

        holder.getView<TextView>(R.id.tvCancel).setOnClickListener {
            holder.getView<SwipeLayout>(R.id.swipeLayout).close()
            onTaskManageListener?.onCancel(task)
        }
        holder.getView<TextView>(R.id.tvEdit).setOnClickListener {
            holder.getView<SwipeLayout>(R.id.swipeLayout).close()
            onTaskManageListener?.onEdit(task)
        }

        if (task.color != 0) {
            try {
//                holder.llContent.setBackgroundColor(Color.parseColor("#"+ ConvertUtils.toColorString(task.color, false)))
                val background = holder.getView<LinearLayout>(R.id.llContent).background as GradientDrawable
                background.setColor(Color.parseColor("#" + ConvertUtils.toColorString(task.color, false)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        holder.getView<LinearLayout>(R.id.llContent).setOnClickListener {
            if ((-10000).toString().equals(task.id)) {
                toast(context, "点击\"+\"号添加提醒吧")
            } else {
                ReminderActivity.launch(context, task.id, -1, true)
            }

        }

        holder.getView<LinearLayout>(R.id.llContent).setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("请选择操作？")
                .setNeutralButton("取消") { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("删除") { dialog, _ ->
                    dialog.dismiss()
                    onTaskManageListener?.onDel(task)
                }
                .setPositiveButton("编辑") { dialog, _ ->
                    dialog.dismiss()
                    onTaskManageListener?.onEdit(task)
                }
                .create().show()
            true
        }
    }


    interface OnTaskManageListener {
        fun onDel(task: Task)
        fun onEdit(task: Task)
        fun onCancel(task: Task)
    }
}