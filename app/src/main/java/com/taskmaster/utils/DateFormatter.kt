package com.taskmaster.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun format(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }
}
