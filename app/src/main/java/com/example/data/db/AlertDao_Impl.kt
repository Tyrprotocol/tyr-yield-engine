// File: AlertDao_Impl.kt
package com.example.data.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.AlertType
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

class AlertDao_Impl(
    private val __db: RoomDatabase
) : AlertDao {

  private val __insertAdapterOfAlertRule: EntityInsertAdapter<AlertRule> =
      object : EntityInsertAdapter<AlertRule>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `alert_rules` (`id`,`type`,`targetTicker`,`thresholdValue`,`comparison`,`isEnabled`,`label`,`lastTriggeredTimestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: AlertRule) {
          statement.bindLong(1, entity.id)
          statement.bindText(2, alertTypeEnumToString(entity.type))
          val targetTicker = entity.targetTicker
          if (targetTicker == null) {
            statement.bindNull(3)
          } else {
            statement.bindText(3, targetTicker)
          }
          statement.bindDouble(4, entity.thresholdValue)
          statement.bindText(5, entity.comparison)
          statement.bindLong(6, if (entity.isEnabled) 1L else 0L)
          statement.bindText(7, entity.label)
          statement.bindLong(8, entity.lastTriggeredTimestamp)
        }
      }

  private val __insertAdapterOfAlertNotification: EntityInsertAdapter<AlertNotification> =
      object : EntityInsertAdapter<AlertNotification>() {
        override fun createQuery(): String =
            "INSERT OR REPLACE INTO `alert_notifications` (`id`,`timestamp`,`title`,`message`,`severity`,`isRead`) VALUES (nullif(?, 0),?,?,?,?,?)"

        override fun bind(statement: SQLiteStatement, entity: AlertNotification) {
          statement.bindLong(1, entity.id)
          statement.bindLong(2, entity.timestamp)
          statement.bindText(3, entity.title)
          statement.bindText(4, entity.message)
          statement.bindText(5, entity.severity)
          statement.bindLong(6, if (entity.isRead) 1L else 0L)
        }
      }

  private val __deleteAdapterOfAlertRule: EntityDeleteOrUpdateAdapter<AlertRule> =
      object : EntityDeleteOrUpdateAdapter<AlertRule>() {
        override fun createQuery(): String = "DELETE FROM `alert_rules` WHERE `id` = ?"

        override fun bind(statement: SQLiteStatement, entity: AlertRule) {
          statement.bindLong(1, entity.id)
        }
      }

  private val __updateAdapterOfAlertRule: EntityDeleteOrUpdateAdapter<AlertRule> =
      object : EntityDeleteOrUpdateAdapter<AlertRule>() {
        override fun createQuery(): String =
            "UPDATE OR ABORT `alert_rules` SET `id` = ?,`type` = ?,`targetTicker` = ?,`thresholdValue` = ?,`comparison` = ?,`isEnabled` = ?,`label` = ?,`lastTriggeredTimestamp` = ? WHERE `id` = ?"

        override fun bind(statement: SQLiteStatement, entity: AlertRule) {
          statement.bindLong(1, entity.id)
          statement.bindText(2, alertTypeEnumToString(entity.type))
          val targetTicker = entity.targetTicker
          if (targetTicker == null) {
            statement.bindNull(3)
          } else {
            statement.bindText(3, targetTicker)
          }
          statement.bindDouble(4, entity.thresholdValue)
          statement.bindText(5, entity.comparison)
          statement.bindLong(6, if (entity.isEnabled) 1L else 0L)
          statement.bindText(7, entity.label)
          statement.bindLong(8, entity.lastTriggeredTimestamp)
          statement.bindLong(9, entity.id)
        }
      }

  override suspend fun insertAlertRule(rule: AlertRule): Long =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfAlertRule.insertAndReturnId(connection, rule)
      }

  override suspend fun insertNotification(notification: AlertNotification): Long =
      performSuspending(__db, false, true) { connection ->
        __insertAdapterOfAlertNotification.insertAndReturnId(connection, notification)
      }

  override suspend fun deleteAlertRule(rule: AlertRule): Unit =
      performSuspending(__db, false, true) { connection ->
        __deleteAdapterOfAlertRule.handle(connection, rule)
      }

  override suspend fun updateAlertRule(rule: AlertRule): Unit =
      performSuspending(__db, false, true) { connection ->
        __updateAdapterOfAlertRule.handle(connection, rule)
      }

  override fun getAllAlertRules(): Flow<List<AlertRule>> {
    val sql = "SELECT * FROM alert_rules ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("alert_rules")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colType = getColumnIndexOrThrow(stmt, "type")
        val colTargetTicker = getColumnIndexOrThrow(stmt, "targetTicker")
        val colThresholdValue = getColumnIndexOrThrow(stmt, "thresholdValue")
        val colComparison = getColumnIndexOrThrow(stmt, "comparison")
        val colIsEnabled = getColumnIndexOrThrow(stmt, "isEnabled")
        val colLabel = getColumnIndexOrThrow(stmt, "label")
        val colLastTriggeredTimestamp = getColumnIndexOrThrow(stmt, "lastTriggeredTimestamp")
        val result = mutableListOf<AlertRule>()
        while (stmt.step()) {
          val targetTicker = if (stmt.isNull(colTargetTicker)) null else stmt.getText(colTargetTicker)
          result.add(
              AlertRule(
                  id = stmt.getLong(colId),
                  type = alertTypeStringToEnum(stmt.getText(colType)),
                  targetTicker = targetTicker,
                  thresholdValue = stmt.getDouble(colThresholdValue),
                  comparison = stmt.getText(colComparison),
                  isEnabled = stmt.getLong(colIsEnabled).toInt() != 0,
                  label = stmt.getText(colLabel),
                  lastTriggeredTimestamp = stmt.getLong(colLastTriggeredTimestamp)
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override fun getAllNotifications(): Flow<List<AlertNotification>> {
    val sql = "SELECT * FROM alert_notifications ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("alert_notifications")) { connection ->
      val stmt = connection.prepare(sql)
      try {
        val colId = getColumnIndexOrThrow(stmt, "id")
        val colTimestamp = getColumnIndexOrThrow(stmt, "timestamp")
        val colTitle = getColumnIndexOrThrow(stmt, "title")
        val colMessage = getColumnIndexOrThrow(stmt, "message")
        val colSeverity = getColumnIndexOrThrow(stmt, "severity")
        val colIsRead = getColumnIndexOrThrow(stmt, "isRead")
        val result = mutableListOf<AlertNotification>()
        while (stmt.step()) {
          result.add(
              AlertNotification(
                  id = stmt.getLong(colId),
                  timestamp = stmt.getLong(colTimestamp),
                  title = stmt.getText(colTitle),
                  message = stmt.getText(colMessage),
                  severity = stmt.getText(colSeverity),
                  isRead = stmt.getLong(colIsRead).toInt() != 0
              )
          )
        }
        result
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun markNotificationAsRead(id: Long) {
    val sql = "UPDATE alert_notifications SET isRead = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.bindLong(1, id)
        stmt.step()
      } finally {
        stmt.close()
      }
    }
  }

  override suspend fun clearAllNotifications() {
    val sql = "DELETE FROM alert_notifications"
    return performSuspending(__db, false, true) { connection ->
      val stmt = connection.prepare(sql)
      try {
        stmt.step()
      } finally {
        stmt.close()
      }
    }
  }

  private fun alertTypeEnumToString(value: AlertType): String = when (value) {
    AlertType.PORTFOLIO_CHANGE_PCT -> "PORTFOLIO_CHANGE_PCT"
    AlertType.ASSET_PRICE_MOVEMENT -> "ASSET_PRICE_MOVEMENT"
    AlertType.DIVIDEND_PAYMENT -> "DIVIDEND_PAYMENT"
    AlertType.DEFENSE_RATIO_TARGET -> "DEFENSE_RATIO_TARGET"
  }

  private fun alertTypeStringToEnum(value: String): AlertType = when (value) {
    "PORTFOLIO_CHANGE_PCT" -> AlertType.PORTFOLIO_CHANGE_PCT
    "ASSET_PRICE_MOVEMENT" -> AlertType.ASSET_PRICE_MOVEMENT
    "DIVIDEND_PAYMENT" -> AlertType.DIVIDEND_PAYMENT
    "DEFENSE_RATIO_TARGET" -> AlertType.DEFENSE_RATIO_TARGET
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: $value")
  }

  companion object {
    fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
