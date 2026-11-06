package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Query("SELECT * FROM study_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE completedSuccessfully = 1 ORDER BY startTimeMillis DESC")
    fun getCompletedSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(actualSecondsFocused) / 60 FROM study_sessions WHERE completedSuccessfully = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE completedSuccessfully = 1")
    fun getCompletedCount(): Flow<Int>

    @Delete
    suspend fun deleteSession(session: StudySession)
}
