package com.upfinder.voicetodo.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.upfinder.voicetodo.R
import com.upfinder.voicetodo.data.entitys.Task
import java.util.ArrayList

class EventAdapter(var context:Context,var events: ArrayList<String>) :BaseAdapter() {

    private lateinit var onEventManageListener: EventAdapter.OnEventManageListener
    private  var onEdit :Boolean = true ;

    fun setOnManagerListener(onEventManageListener: EventAdapter.OnEventManageListener) {
        this.onEventManageListener = onEventManageListener
    }

    fun setEdit(onEdit : Boolean){
        this.onEdit = onEdit;
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view :View?
        val viewHolder : ViewHolder
        if (convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            // 需要创建视图 animals_list
            view = inflater.inflate(R.layout.item_event_layout, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        }else{
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.delete?.setOnClickListener{
            onEventManageListener.onDel(events.get(position),position);
        }

        viewHolder.delete?.visibility = if(onEdit) View.VISIBLE else View.GONE;

        viewHolder.name?.text = events.get(position)
        return  view as View;

    }

    override fun getItem(position: Int): String {
        return events.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return events.size
    }

    private class ViewHolder(view: View?) {
        var name: TextView? = null
        var status : TextView?= null
        var delete : Button? =null
        init {
            this.name = view?.findViewById(R.id.event_name)
            this.status = view?.findViewById(R.id.event_status)
            this.delete = view?.findViewById(R.id.event_delete)
        }
    }

    interface OnEventManageListener {
        fun onDel(event: String,index : Int)
    }
}