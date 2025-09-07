package com.skillsnap.app.di

import android.content.Context
import androidx.room.Room
import com.skillsnap.app.data.database.ChallengeDao
import com.skillsnap.app.data.database.SkillSnapDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideSkillSnapDatabase(@ApplicationContext context: Context): SkillSnapDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SkillSnapDatabase::class.java,
            "skillsnap_database"
        ).build()
    }
    
    @Provides
    fun provideChallengeDao(database: SkillSnapDatabase): ChallengeDao {
        return database.challengeDao()
    }
}