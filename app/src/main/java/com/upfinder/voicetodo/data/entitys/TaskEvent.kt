package com.upfinder.voicetodo.data.entitys

/**
 * Created by lgx on 2019/6/14.
 */
class TaskEvent constructor(
    var task:Task, //对应的任务
    var index:Int ?= -1, //对应事件
    var event:String,
    var state:Int
)
{

    fun changeState(){

    }
}