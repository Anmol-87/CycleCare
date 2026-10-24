package com.anmol.cyclecare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PeriodEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CycleCareDatabase : RoomDatabase() {

    abstract fun periodDao(): PeriodDao

    companion object {
        @Volatile
        private var INSTANCE: CycleCareDatabase? = null

        fun getDatabase(context: Context): CycleCareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CycleCareDatabase::class.java,
                    "cyclecare_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}