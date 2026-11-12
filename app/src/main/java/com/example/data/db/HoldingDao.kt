// File: HoldingDao.kt
package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Holding
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings ORDER BY stage ASC, ticker ASC")
    fun getAllHoldings(): Flow<List<Holding>>

    @Query("SELECT * FROM holdings WHERE stage = :stage ORDER BY ticker ASC")
    fun getHoldingsByStage(stage: Int): Flow<List<Holding>>

    @Query("SELECT * FROM holdings WHERE ticker = :ticker LIMIT 1")
    suspend fun getHoldingByTicker(ticker: String): Holding?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holdings: List<Holding>)

    @Update
    suspend fun updateHolding(holding: Holding)

    @Delete
    suspend fun deleteHolding(holding: Holding)

    @Query("DELETE FROM holdings")
    suspend fun deleteAllHoldings()
}
