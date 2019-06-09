package com.upfinder.voicetodo.task

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.upfinder.voicetodo.R
import java.util.ArrayList

class EventAdapter(var context:Context,var events: ArrayList<String>) :BaseAdapter() {

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
        init {
            this.name = view?.findViewById(R.id.event_name)
            this.status = view?.findViewById(R.id.event_status)
        }
    }
}