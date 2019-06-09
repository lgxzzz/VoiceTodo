package com.upfinder.voicetodo.data

import android.arch.persistence.room.*
import com.upfinder.voicetodo.data.entitys.Task

@Dao
interface TasksDao {

    @Query("SELECT * FROM tasks ORDER BY calendar")
    fun getTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id=:taskId ORDER BY calendar")
    fun getTaskById(taskId: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Update
    fun updateTask(task: Task): Int

    @Query("UPDATE tasks SET hasReminder = :hasReminder WHERE id = :taskId")
    fun updateHasReminder(taskId: String, hasReminder: Boolean)

    @Query("DELETE FROM Tasks WHERE id = :taskId")
    fun deleteTaskById(taskId: String): Int


    /**
     * Delete all tasks.
     */
    @Query("DELETE FROM Tasks")
    fun deleteTasks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tasks: List<Task>)
}