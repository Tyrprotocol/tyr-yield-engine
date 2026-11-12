// File: CashDao.kt
package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CashRecord
import com.example.data.model.SweepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_records ORDER BY timestamp DESC")
    fun getAllCashRecords(): Flow<List<CashRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashRecord(record: CashRecord): Long

    @Query("DELETE FROM cash_records")
    suspend fun deleteAllCashRecords()

    @Query("SELECT * FROM sweep_records ORDER BY timestamp DESC")
    fun getAllSweepRecords(): Flow<List<SweepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSweepRecord(record: SweepRecord): Long

    @Query("DELETE FROM sweep_records")
    suspend fun deleteAllSweepRecords()
}
