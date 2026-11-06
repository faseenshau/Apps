package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 30,
    val actualSecondsFocused: Int = 0,
    val subjectGoal: String = "Study Session",
    val blockedAppsCount: Int = 0,
    val completedSuccessfully: Boolean = true,
    val notes: String = ""
)
