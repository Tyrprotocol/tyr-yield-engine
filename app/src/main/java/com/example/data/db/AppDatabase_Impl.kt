// File: AppDatabase_Impl.kt
package com.example.data.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.reflect.KClass

class AppDatabase_Impl : AppDatabase() {
  private val _holdingDao: Lazy<HoldingDao> = lazy {
    HoldingDao_Impl(this)
  }

  private val _cashDao: Lazy<CashDao> = lazy {
    CashDao_Impl(this)
  }

  private val _alertDao: Lazy<AlertDao> = lazy {
    AlertDao_Impl(this)
  }

  override fun createOpenDelegate(): RoomOpenDelegate {
    return object : RoomOpenDelegate(
        2,
        "49b26a894438cdd5c38cf35b2f6e80f6",
        "5d491faea20e202aeb555c8efadebaf6"
    ) {
      override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `holdings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ticker` TEXT NOT NULL, `name` TEXT NOT NULL, `shares` REAL NOT NULL, `avgCost` REAL NOT NULL, `currentPrice` REAL NOT NULL, `annualYieldPercent` REAL NOT NULL, `stage` INTEGER NOT NULL, `categoryTag` TEXT NOT NULL, `distributionFrequency` TEXT NOT NULL, `notes` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `cash_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `sourceTicker` TEXT, `note` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sweep_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `totalCashDeployed` REAL NOT NULL, `targetsSummary` TEXT NOT NULL, `blendedYieldAchieved` REAL NOT NULL, `rationale` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `alert_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `targetTicker` TEXT, `thresholdValue` REAL NOT NULL, `comparison` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `label` TEXT NOT NULL, `lastTriggeredTimestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `alert_notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `severity` TEXT NOT NULL, `isRead` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '49b26a894438cdd5c38cf35b2f6e80f6')")
      }

      override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `holdings`")
        connection.execSQL("DROP TABLE IF EXISTS `cash_records`")
        connection.execSQL("DROP TABLE IF EXISTS `sweep_records`")
        connection.execSQL("DROP TABLE IF EXISTS `alert_rules`")
        connection.execSQL("DROP TABLE IF EXISTS `alert_notifications`")
      }

      override fun onCreate(connection: SQLiteConnection) {}

      override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      override fun onPostMigrate(connection: SQLiteConnection) {}

      override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val columnsHoldings = mutableMapOf<String, TableInfo.Column>()
        columnsHoldings["id"] = TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["ticker"] = TableInfo.Column("ticker", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["name"] = TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["shares"] = TableInfo.Column("shares", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["avgCost"] = TableInfo.Column("avgCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["currentPrice"] = TableInfo.Column("currentPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["annualYieldPercent"] = TableInfo.Column("annualYieldPercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["stage"] = TableInfo.Column("stage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["categoryTag"] = TableInfo.Column("categoryTag", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["distributionFrequency"] = TableInfo.Column("distributionFrequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsHoldings["notes"] = TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        val infoHoldings = TableInfo("holdings", columnsHoldings, emptySet(), emptySet())
        val existingHoldings = read(connection, "holdings")
        if (infoHoldings != existingHoldings) {
          return RoomOpenDelegate.ValidationResult(false, "holdings(com.example.data.model.Holding).\n Expected:\n$infoHoldings\n Found:\n$existingHoldings")
        }

        val columnsCashRecords = mutableMapOf<String, TableInfo.Column>()
        columnsCashRecords["id"] = TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
        columnsCashRecords["timestamp"] = TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsCashRecords["amount"] = TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsCashRecords["type"] = TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsCashRecords["sourceTicker"] = TableInfo.Column("sourceTicker", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsCashRecords["note"] = TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        val infoCashRecords = TableInfo("cash_records", columnsCashRecords, emptySet(), emptySet())
        val existingCashRecords = read(connection, "cash_records")
        if (infoCashRecords != existingCashRecords) {
          return RoomOpenDelegate.ValidationResult(false, "cash_records(com.example.data.model.CashRecord).\n Expected:\n$infoCashRecords\n Found:\n$existingCashRecords")
        }

        val columnsSweepRecords = mutableMapOf<String, TableInfo.Column>()
        columnsSweepRecords["id"] = TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
        columnsSweepRecords["timestamp"] = TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsSweepRecords["totalCashDeployed"] = TableInfo.Column("totalCashDeployed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsSweepRecords["targetsSummary"] = TableInfo.Column("targetsSummary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsSweepRecords["blendedYieldAchieved"] = TableInfo.Column("blendedYieldAchieved", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsSweepRecords["rationale"] = TableInfo.Column("rationale", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        val infoSweepRecords = TableInfo("sweep_records", columnsSweepRecords, emptySet(), emptySet())
        val existingSweepRecords = read(connection, "sweep_records")
        if (infoSweepRecords != existingSweepRecords) {
          return RoomOpenDelegate.ValidationResult(false, "sweep_records(com.example.data.model.SweepRecord).\n Expected:\n$infoSweepRecords\n Found:\n$existingSweepRecords")
        }

        val columnsAlertRules = mutableMapOf<String, TableInfo.Column>()
        columnsAlertRules["id"] = TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["type"] = TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["targetTicker"] = TableInfo.Column("targetTicker", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["thresholdValue"] = TableInfo.Column("thresholdValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["comparison"] = TableInfo.Column("comparison", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["isEnabled"] = TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["label"] = TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertRules["lastTriggeredTimestamp"] = TableInfo.Column("lastTriggeredTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        val infoAlertRules = TableInfo("alert_rules", columnsAlertRules, emptySet(), emptySet())
        val existingAlertRules = read(connection, "alert_rules")
        if (infoAlertRules != existingAlertRules) {
          return RoomOpenDelegate.ValidationResult(false, "alert_rules(com.example.data.model.AlertRule).\n Expected:\n$infoAlertRules\n Found:\n$existingAlertRules")
        }

        val columnsAlertNotifications = mutableMapOf<String, TableInfo.Column>()
        columnsAlertNotifications["id"] = TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertNotifications["timestamp"] = TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertNotifications["title"] = TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertNotifications["message"] = TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertNotifications["severity"] = TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        columnsAlertNotifications["isRead"] = TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
        val infoAlertNotifications = TableInfo("alert_notifications", columnsAlertNotifications, emptySet(), emptySet())
        val existingAlertNotifications = read(connection, "alert_notifications")
        if (infoAlertNotifications != existingAlertNotifications) {
          return RoomOpenDelegate.ValidationResult(false, "alert_notifications(com.example.data.model.AlertNotification).\n Expected:\n$infoAlertNotifications\n Found:\n$existingAlertNotifications")
        }

        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
  }

  override fun createInvalidationTracker(): InvalidationTracker {
    return InvalidationTracker(
        this,
        emptyMap(),
        emptyMap(),
        "holdings",
        "cash_records",
        "sweep_records",
        "alert_rules",
        "alert_notifications"
    )
  }

  override fun clearAllTables() {
    super.performClear(
        false,
        "holdings",
        "cash_records",
        "sweep_records",
        "alert_rules",
        "alert_notifications"
    )
  }

  override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val typeConvertersMap = mutableMapOf<KClass<*>, List<KClass<*>>>()
    typeConvertersMap[HoldingDao::class] = HoldingDao_Impl.getRequiredConverters()
    typeConvertersMap[CashDao::class] = CashDao_Impl.getRequiredConverters()
    typeConvertersMap[AlertDao::class] = AlertDao_Impl.getRequiredConverters()
    return typeConvertersMap
  }

  override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    return emptySet()
  }

  override fun createAutoMigrations(
      autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>
  ): List<Migration> {
    return emptyList()
  }

  override fun holdingDao(): HoldingDao = _holdingDao.value

  override fun cashDao(): CashDao = _cashDao.value

  override fun alertDao(): AlertDao = _alertDao.value
}
