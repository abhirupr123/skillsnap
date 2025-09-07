package com.skillsnap.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.skillsnap.app.data.model.Challenge

@Database(
    entities = [Challenge::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SkillSnapDatabase : RoomDatabase() {
    
    abstract fun challengeDao(): ChallengeDao
    
    companion object {
        @Volatile
        private var INSTANCE: SkillSnapDatabase? = null
        
        fun getDatabase(context: Context): SkillSnapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkillSnapDatabase::class.java,
                    "skillsnap_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 