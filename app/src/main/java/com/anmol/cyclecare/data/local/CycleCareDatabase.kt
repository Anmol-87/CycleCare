package com.anmol.cyclecare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PeriodEntity::class,
        CareResponseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CycleCareDatabase : RoomDatabase() {

    abstract fun periodDao(): PeriodDao

    abstract fun careResponseDao(): CareResponseDao

    companion object {
        @Volatile
        private var INSTANCE: CycleCareDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS care_responses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderType TEXT NOT NULL,
                        response TEXT NOT NULL,
                        answeredAt INTEGER NOT NULL,
                        reminderId TEXT NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_care_responses_reminderId
                    ON care_responses (reminderId)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): CycleCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CycleCareDatabase::class.java,
                    "cyclecare_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}