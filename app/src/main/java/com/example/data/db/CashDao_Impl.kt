// File: CashDao_Impl.kt
package com.example.data.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.data.model.CashRecord
import com.example.data.model.SweepRecord
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

class CashDao_Impl(
    private val __db: RoomDatabase
) : CashDao {

  private val __insertAdapterOfCashRecord: EntityInsertAdapter<CashRecord> =
      object : EntityInsertAdapter<CashRecord>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `cash_records` (`id`,`timestamp`,`amount`,`type`,`sourceTicker`,`note`) VALUES (nullif(?, 0),?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: CashRecord) {
          statement.bindLong(1, entity.id)
          statement.bindLong(2, entity.timestamp)
          statement.bindDouble(3, entity.amount)
          statement.bindText(4, entity.type)
          val sourceTicker = entity.sourceTicker
          if (sourceTicker == null) {
            statement.bindNull(5)
          } else {
            statement.bindText(5, sourceTicker)
          }
          statement.bindText(6, entity.note)
        }
      }

  private val __insertAdapterOfSweepRecord: EntityInsertAdapter<SweepRecord> =
      object : EntityInsertAdapter<SweepRecord>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `sweep_records` (`id`,`timestamp`,`totalCashDeployed`,`targetsSummary`,`blendedYieldAchieved`,`rationale`) VALUES (nullif(?, 0),?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: SweepRecord) {
          statement.bindLong(1, entity.id)
          statement.bindLong(2, entity.timestamp)
          statement.bindDouble(3, entity.totalCashDeployed)
          statement.bindText(4, entity.targetsSummary)
          statement.bindDouble(5, entity.blendedYieldAchieved)
          statement.bindText(6, entity.rationale)
        }
      }

  override suspend fun insertCashRecord(record: CashRecord): Long =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfCashRecord.insertAndReturnId(connection, record)
      }

  override suspend fun insertSweepRecord(record: SweepRecord): Long =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfSweepRecord.insertAndReturnId(connection, record)
      }

  override fun getAllCashRecords(): Flow<List<CashRecord>> {
    val sql = "SELECT * FROM cash_records ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("cash_records")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTimestamp = getColumnIndexOrThrow(stmt, "timestamp")
        val colAmount = getColumnIndexOrThrow(stmt, "amount")
        val colType = getColumnIndexOrThrow(stmt, "type")
        val colSourceTicker = getColumnIndexOrThrow(stmt, "sourceTicker")
        val colNote = getColumnIndexOrThrow(stmt, "note")
        val result = mutableListOf<CashRecord>()
        while (stmt.step()) {
          val sourceTicker = if (stmt.isNull(colSourceTicker)) null else stmt.getText(colSourceTicker)
          result.add(
              CashRecord(
                  id = stmt.getLong(colId),
                  timestamp = stmt.getLong(colTimestamp),
                  amount = stmt.getDouble(colAmount),
                  type = stmt.getText(colType),
                  sourceTicker = sourceTicker,
                  note = stmt.getText(colNote)
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override fun getAllSweepRecords(): Flow<List<SweepRecord>> {
    val sql = "SELECT * FROM sweep_records ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("sweep_records")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTimestamp = getColumnIndexOrThrow(stmt, "timestamp")
        val colTotalCashDeployed = getColumnIndexOrThrow(stmt, "totalCashDeployed")
        val colTargetsSummary = getColumnIndexOrThrow(stmt, "targetsSummary")
        val colBlendedYieldAchieved = getColumnIndexOrThrow(stmt, "blendedYieldAchieved")
        val colRationale = getColumnIndexOrThrow(stmt, "rationale")
        val result = mutableListOf<SweepRecord>()
        while (stmt.step()) {
          result.add(
              SweepRecord(
                  id = stmt.getLong(colId),
                  timestamp = stmt.getLong(colTimestamp),
                  totalCashDeployed = stmt.getDouble(colTotalCashDeployed),
                  targetsSummary = stmt.getText(colTargetsSummary),
                  blendedYieldAchieved = stmt.getDouble(colBlendedYieldAchieved),
                  rationale = stmt.getText(colRationale)
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun deleteAllCashRecords() {
    val sql = "DELETE FROM cash_records"
    return performSuspending(__db, false, true) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.step()
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun deleteAllSweepRecords() {
    val sql = "DELETE FROM sweep_records"
    return performSuspending(__db, false, true) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.step()
      } finally {
        stmt.close()
      }
    }
  }

  companion object {
    fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
