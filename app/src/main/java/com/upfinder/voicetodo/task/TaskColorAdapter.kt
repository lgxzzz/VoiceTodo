package com.upfinder.voicetodo.task

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.view.CirclePointView

class TaskColorAdapter(var context: Context) : RecyclerView.Adapter<TaskColorAdapter.ViewHolder>() {

    private var colors: ArrayList<Int> = arrayListOf()
    private  var onChoiceListener: OnChoiceListener? = null

    init {
        colors.add(R.color.task_1)
        colors.add(R.color.task_2)
        colors.add(R.color.task_3)
        colors.add(R.color.task_4)
        colors.add(R.color.task_5)
        colors.add(R.color.task_6)
        colors.add(R.color.task_7)
        colors.add(R.color.task_8)
        colors.add(R.color.task_9)
        colors.add(R.color.task_10)
        colors.add(R.color.task_11)
        colors.add(R.color.task_12)
        colors.add(R.color.task_13)
        colors.add(R.color.task_14)
        colors.add(R.color.task_15)
        colors.add(R.color.task_16)
        colors.add(R.color.task_17)
        colors.add(R.color.task_18)
        colors.add(R.color.task_19)
        colors.add(R.color.task_20)
        colors.add(R.color.task_21)
        colors.add(R.color.task_22)
        colors.add(R.color.task_23)
        colors.add(R.color.task_24)

    }


    fun setOnChoiceListener(onChoiceListener: OnChoiceListener) {
        this.onChoiceListener = onChoiceListener
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_task_color_layout, p0, false))

    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
        holder.circlePointView.setColor(context.resources.getColor(colors[p1])).setOnClickListener {
            onChoiceListener?.onSelected(context.resources.getColor(colors[p1]))
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circlePointView = itemView.findViewById<CirclePointView>(R.id.circlePointView)


    }


    interface OnChoiceListener {
        fun onSelected(colorRes: Int)
    }
}