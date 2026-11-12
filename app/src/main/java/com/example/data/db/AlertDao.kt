// File: AlertDao.kt
package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_rules ORDER BY id DESC")
    fun getAllAlertRules(): Flow<List<AlertRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertRule(rule: AlertRule): Long

    @Update
    suspend fun updateAlertRule(rule: AlertRule)

    @Delete
    suspend fun deleteAlertRule(rule: AlertRule)

    @Query("SELECT * FROM alert_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AlertNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AlertNotification): Long

    @Query("UPDATE alert_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("DELETE FROM alert_notifications")
    suspend fun clearAllNotifications()
}
