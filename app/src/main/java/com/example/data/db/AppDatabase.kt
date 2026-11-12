// File: AppDatabase.kt
package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.CashRecord
import com.example.data.model.Holding
import com.example.data.model.SweepRecord

@Database(
    entities = [
        Holding::class,
        CashRecord::class,
        SweepRecord::class,
        AlertRule::class,
        AlertNotification::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao
    abstract fun cashDao(): CashDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yield_engine_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
