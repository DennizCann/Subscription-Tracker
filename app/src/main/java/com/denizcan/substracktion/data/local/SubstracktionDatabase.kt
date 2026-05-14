package com.denizcan.substracktion.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SubstracktionDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var instance: SubstracktionDatabase? = null

        fun getInstance(context: Context): SubstracktionDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SubstracktionDatabase::class.java,
                    "substracktion.db"
                ).build().also { instance = it }
            }
    }
}
