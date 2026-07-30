package com.taskmaster.domain.model

data class Task(
    val id: Long = 0L,
    val remoteId: Int? = null,
    val title: String,
    val description: String,
    val dueDate: Long,
    val priority: Priority,
    val status: Status,
    val syncState: SyncState = SyncState.SYNCED
)

enum class Priority {
    LOW, MEDIUM, HIGH;

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class Status {
    PENDING, COMPLETED;

    companion object {
        fun fromString(value: String): Status {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class SyncState {
    SYNCED, PENDING_INSERT, PENDING_UPDATE, PENDING_DELETE;

    companion object {
        fun fromString(value: String): SyncState {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SYNCED
        }
    }
}
