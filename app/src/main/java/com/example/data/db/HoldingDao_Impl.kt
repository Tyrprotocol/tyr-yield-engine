// File: HoldingDao_Impl.kt
package com.example.data.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.data.model.Holding
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

class HoldingDao_Impl(
    private val __db: RoomDatabase
) : HoldingDao {

  private val __insertAdapterOfHolding: EntityInsertAdapter<Holding> =
      object : EntityInsertAdapter<Holding>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `holdings` (`id`,`ticker`,`name`,`shares`,`avgCost`,`currentPrice`,`annualYieldPercent`,`stage`,`categoryTag`,`distributionFrequency`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: Holding) {
          statement.bindLong(1, entity.id)
          statement.bindText(2, entity.ticker)
          statement.bindText(3, entity.name)
          statement.bindDouble(4, entity.shares)
          statement.bindDouble(5, entity.avgCost)
          statement.bindDouble(6, entity.currentPrice)
          statement.bindDouble(7, entity.annualYieldPercent)
          statement.bindLong(8, entity.stage.toLong())
          statement.bindText(9, entity.categoryTag)
          statement.bindText(10, entity.distributionFrequency)
          statement.bindText(11, entity.notes)
        }
      }

  private val __deleteAdapterOfHolding: EntityDeleteOrUpdateAdapter<Holding> =
      object : EntityDeleteOrUpdateAdapter<Holding>() {
        override fun createQuery(): String = "DELETE FROM `holdings` WHERE `id` = ?"

        override fun bind(statement: SQLiteStatement, entity: Holding) {
          statement.bindLong(1, entity.id)
        }
      }

  private val __updateAdapterOfHolding: EntityDeleteOrUpdateAdapter<Holding> =
      object : EntityDeleteOrUpdateAdapter<Holding>() {
        override fun createQuery(): String =
            "UPDATE OR ABORT `holdings` SET `id` = ?,`ticker` = ?,`name` = ?,`shares` = ?,`avgCost` = ?,`currentPrice` = ?,`annualYieldPercent` = ?,`stage` = ?,`categoryTag` = ?,`distributionFrequency` = ?,`notes` = ? WHERE `id` = ?"

        override fun bind(statement: SQLiteStatement, entity: Holding) {
          statement.bindLong(1, entity.id)
          statement.bindText(2, entity.ticker)
          statement.bindText(3, entity.name)
          statement.bindDouble(4, entity.shares)
          statement.bindDouble(5, entity.avgCost)
          statement.bindDouble(6, entity.currentPrice)
          statement.bindDouble(7, entity.annualYieldPercent)
          statement.bindLong(8, entity.stage.toLong())
          statement.bindText(9, entity.categoryTag)
          statement.bindText(10, entity.distributionFrequency)
          statement.bindText(11, entity.notes)
          statement.bindLong(12, entity.id)
        }
      }

  override suspend fun insertHolding(holding: Holding): Long =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfHolding.insertAndReturnId(connection, holding)
      }

  override suspend fun insertAll(holdings: List<Holding>): Unit =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfHolding.insert(connection, holdings)
      }

  override suspend fun deleteHolding(holding: Holding): Unit =
      performSuspending(__db, false, true) { connection ->
        __deleteAdapterOfHolding.handle(connection, holding)
      }

  override suspend fun updateHolding(holding: Holding): Unit =
      performSuspending(__db, false, true) { connection ->
        __updateAdapterOfHolding.handle(connection, holding)
      }

  override fun getAllHoldings(): Flow<List<Holding>> {
    val sql = "SELECT * FROM holdings ORDER BY stage ASC, ticker ASC"
    return createFlow(__db, false, arrayOf("holdings")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTicker = getColumnIndexOrThrow(stmt, "ticker")
        val colName = getColumnIndexOrThrow(stmt, "name")
        val colShares = getColumnIndexOrThrow(stmt, "shares")
        val colAvgCost = getColumnIndexOrThrow(stmt, "avgCost")
        val colCurrentPrice = getColumnIndexOrThrow(stmt, "currentPrice")
        val colAnnualYieldPercent = getColumnIndexOrThrow(stmt, "annualYieldPercent")
        val colStage = getColumnIndexOrThrow(stmt, "stage")
        val colCategoryTag = getColumnIndexOrThrow(stmt, "categoryTag")
        val colDistributionFrequency = getColumnIndexOrThrow(stmt, "distributionFrequency")
        val colNotes = getColumnIndexOrThrow(stmt, "notes")
        val result = mutableListOf<Holding>()
        while (stmt.step()) {
          result.add(
              Holding(
                  id = stmt.getLong(colId),
                  ticker = stmt.getText(colTicker),
                  name = stmt.getText(colName),
                  shares = stmt.getDouble(colShares),
                  avgCost = stmt.getDouble(colAvgCost),
                  currentPrice = stmt.getDouble(colCurrentPrice),
                  annualYieldPercent = stmt.getDouble(colAnnualYieldPercent),
                  stage = stmt.getLong(colStage).toInt(),
                  categoryTag = stmt.getText(colCategoryTag),
                  distributionFrequency = stmt.getText(colDistributionFrequency),
                  notes = stmt.getText(colNotes)
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override fun getHoldingsByStage(stage: Int): Flow<List<Holding>> {
    val sql = "SELECT * FROM holdings WHERE stage = ? ORDER BY ticker ASC"
    return createFlow(__db, false, arrayOf("holdings")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.bindLong(1, stage.toLong())
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTicker = getColumnIndexOrThrow(stmt, "ticker")
        val colName = getColumnIndexOrThrow(stmt, "name")
        val colShares = getColumnIndexOrThrow(stmt, "shares")
        val colAvgCost = getColumnIndexOrThrow(stmt, "avgCost")
        val colCurrentPrice = getColumnIndexOrThrow(stmt, "currentPrice")
        val colAnnualYieldPercent = getColumnIndexOrThrow(stmt, "annualYieldPercent")
        val colStage = getColumnIndexOrThrow(stmt, "stage")
        val colCategoryTag = getColumnIndexOrThrow(stmt, "categoryTag")
        val colDistributionFrequency = getColumnIndexOrThrow(stmt, "distributionFrequency")
        val colNotes = getColumnIndexOrThrow(stmt, "notes")
        val result = mutableListOf<Holding>()
        while (stmt.step()) {
          result.add(
              Holding(
                  id = stmt.getLong(colId),
                  ticker = stmt.getText(colTicker),
                  name = stmt.getText(colName),
                  shares = stmt.getDouble(colShares),
                  avgCost = stmt.getDouble(colAvgCost),
                  currentPrice = stmt.getDouble(colCurrentPrice),
                  annualYieldPercent = stmt.getDouble(colAnnualYieldPercent),
                  stage = stmt.getLong(colStage).toInt(),
                  categoryTag = stmt.getText(colCategoryTag),
                  distributionFrequency = stmt.getText(colDistributionFrequency),
                  notes = stmt.getText(colNotes)
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun getHoldingByTicker(ticker: String): Holding? {
    val sql = "SELECT * FROM holdings WHERE ticker = ? LIMIT 1"
    return performSuspending(__db, true, false) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.bindText(1, ticker)
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTicker = getColumnIndexOrThrow(stmt, "ticker")
        val colName = getColumnIndexOrThrow(stmt, "name")
        val colShares = getColumnIndexOrThrow(stmt, "shares")
        val colAvgCost = getColumnIndexOrThrow(stmt, "avgCost")
        val colCurrentPrice = getColumnIndexOrThrow(stmt, "currentPrice")
        val colAnnualYieldPercent = getColumnIndexOrThrow(stmt, "annualYieldPercent")
        val colStage = getColumnIndexOrThrow(stmt, "stage")
        val colCategoryTag = getColumnIndexOrThrow(stmt, "categoryTag")
        val colDistributionFrequency = getColumnIndexOrThrow(stmt, "distributionFrequency")
        val colNotes = getColumnIndexOrThrow(stmt, "notes")
        if (stmt.step()) {
          Holding(
              id = stmt.getLong(colId),
              ticker = stmt.getText(colTicker),
              name = stmt.getText(colName),
              shares = stmt.getDouble(colShares),
              avgCost = stmt.getDouble(colAvgCost),
              currentPrice = stmt.getDouble(colCurrentPrice),
              annualYieldPercent = stmt.getDouble(colAnnualYieldPercent),
              stage = stmt.getLong(colStage).toInt(),
              categoryTag = stmt.getText(colCategoryTag),
              distributionFrequency = stmt.getText(colDistributionFrequency),
              notes = stmt.getText(colNotes)
          )
        } else {
          null
        }
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun deleteAllHoldings() {
    val sql = "DELETE FROM holdings"
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
