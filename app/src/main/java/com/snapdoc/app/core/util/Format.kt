package com.snapdoc.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
    else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024 * 1024))
}

private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
private val yearFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

fun formatTimestamp(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val ageMs = abs(now - epochMillis)
    val day = 24 * 60 * 60 * 1000L
    return when {
        ageMs < day -> "Today, ${timeFmt.format(Date(epochMillis))}"
        ageMs < 2 * day -> "Yesterday"
        ageMs < 365 * day -> dateFmt.format(Date(epochMillis))
        else -> yearFmt.format(Date(epochMillis))
    }
}

fun formatMetadata(pageCount: Int, sizeBytes: Long, createdAt: Long): String {
    val pages = if (pageCount == 1) "1 page" else "$pageCount pages"
    return "$pages · ${formatFileSize(sizeBytes)} · ${formatTimestamp(createdAt)}"
}
