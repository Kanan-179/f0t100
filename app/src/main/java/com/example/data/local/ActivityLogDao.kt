package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getLogsForDate(dateString: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Delete
    suspend fun deleteLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun getLogCount(): Int
}
