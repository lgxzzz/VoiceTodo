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
import com.upfinder.voicetodo.utils.toast


class TaskAdapter(var context: Context, data: List<Task>?) :
    BaseQuickAdapter<Task, BaseViewHolder>(R.layout.item_task_layout, data) {

    private lateinit var onTaskManageListener: TaskAdapter.OnTaskManageListener

    fun setOnManagerListener(onTaskManageListener: TaskAdapter.OnTaskManageListener) {
        this.onTaskManageListener = onTaskManageListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(holder: BaseViewHolder, task: Task) {
        val position = data.indexOf(task) + 1
        holder.getView<TextView>(R.id.tvTitle).text = "事件 $position:" + task.getFormatTitle()
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
//class TaskAdapter(var context: Context, var tasks: ArrayList<Task>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
//
//
//    private lateinit var onTaskManageListener: OnTaskManageListener
//
//    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
//        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_task_layout, p0, false))
//    }
//
//    override fun getItemCount(): Int {
//        return tasks.size
//    }
//
//
//    fun setNewData(datas: ArrayList<Task>) {
//        this.tasks = datas
//        notifyDataSetChanged()
//    }
//
//    fun setOnManagerListener(onTaskManageListener: OnTaskManageListener) {
//        this.onTaskManageListener = onTaskManageListener
//    }
//
//    @SuppressLint("ResourceType", "SetTextI18n")
//    override fun onBindViewHolder(holder: ViewHolder, index: Int) {
//        val task = tasks[index]
////        holder.tvTitle.text = task.title
//        val position = index + 1
//        holder.tvTitle.text = "事件 $position:" + task.getFormatTitle()
////        holder.tvContent.text = task.description
//        holder.tvTime.text = task.getHourMinute()
//        holder.swipeLayout.showMode = SwipeLayout.ShowMode.PullOut
//
//        holder.tvDel.setOnClickListener {
//            onTaskManageListener?.onDel(task)
//        }
//
//        holder.tvCancel.setOnClickListener {
//            holder.swipeLayout.close()
//            onTaskManageListener?.onCancel(task)
//        }
//        holder.tvEdit.setOnClickListener {
//            holder.swipeLayout.close()
//            onTaskManageListener?.onEdit(task)
//        }
//
//        if (task.color != 0) {
//            try {
////                holder.llContent.setBackgroundColor(Color.parseColor("#"+ ConvertUtils.toColorString(task.color, false)))
//                val background = holder.llContent.background as GradientDrawable
//                background.setColor(Color.parseColor("#" + ConvertUtils.toColorString(task.color, false)))
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        holder.tvTitle.setOnClickListener {
//            ReminderActivity.launch(context, task.id, -1, true)
//        }
//
//        holder.tvTitle.setOnLongClickListener {
//            AlertDialog.Builder(context)
//                .setTitle("请选择操作？")
//                .setNeutralButton("取消") { dialog, _ -> dialog.dismiss() }
//                .setNegativeButton("删除") { dialog, _ ->
//                    dialog.dismiss()
//                    onTaskManageListener?.onDel(task)
//                }
//                .setPositiveButton("编辑") { dialog, _ ->
//                    dialog.dismiss()
//                    onTaskManageListener?.onEdit(task)
//                }
//                .create().show()
//            true
//        }
//
//    }
//
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        val swipeLayout = itemView.findViewById<SwipeLayout>(R.id.swipeLayout)
//        val llContent = itemView.findViewById<View>(R.id.llContent)
//        val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
//        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
////        val tvContent = itemView.findViewById<TextView>(R.id.tvContent)
//
//        val tvDel = itemView.findViewById<TextView>(R.id.tvDel)
//        val tvCancel = itemView.findViewById<TextView>(R.id.tvCancel)
//        val tvEdit = itemView.findViewById<TextView>(R.id.tvEdit)
//
//    }
//
//
//    interface OnTaskManageListener {
//        fun onDel(task: Task)
//        fun onEdit(task: Task)
//        fun onCancel(task: Task)
//    }
//}