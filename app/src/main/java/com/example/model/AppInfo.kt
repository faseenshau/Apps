package com.example.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isShielded: Boolean = true,
    val isSystemApp: Boolean = false,
    val category: String = "Distraction"
)
