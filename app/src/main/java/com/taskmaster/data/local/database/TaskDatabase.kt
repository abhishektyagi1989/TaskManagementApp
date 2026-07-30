package com.taskmaster.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.taskmaster.data.local.dao.TaskDao
import com.taskmaster.data.local.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {
    abstract val taskDao: TaskDao

    companion object {
        const val DATABASE_NAME = "taskmaster_db"
    }
}
