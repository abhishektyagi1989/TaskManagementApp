package com.taskmaster.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val remoteId: Int? = null, // ID returned by network/API (e.g. JSONPlaceholder)
    val title: String,
    val description: String,
    val dueDate: Long, // timestamp
    val priority: String, // "Low", "Medium", "High"
    val status: String, // "Pending", "Completed"
    val syncState: String = "SYNCED" // "SYNCED", "PENDING_INSERT", "PENDING_UPDATE", "PENDING_DELETE"
)
