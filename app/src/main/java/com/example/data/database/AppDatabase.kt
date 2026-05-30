package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.TabEntity
import com.example.data.model.RowEntity
import com.example.data.model.ChatMessageEntity

import com.example.data.model.OfficerEntity
import com.example.data.model.StatusOptionEntity
import com.example.data.model.CommTrafficEntity
import com.example.data.model.UserEntity
import com.example.data.model.FuelTransactionEntity
import com.example.data.model.FuelLimitEntity

@Database(
    entities = [
        TabEntity::class, 
        RowEntity::class, 
        ChatMessageEntity::class,
        OfficerEntity::class,
        StatusOptionEntity::class,
        CommTrafficEntity::class,
        UserEntity::class,
        FuelTransactionEntity::class,
        FuelLimitEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tabDao(): TabDao
    abstract fun rowDao(): RowDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun officerDao(): OfficerDao
    abstract fun statusOptionDao(): StatusOptionDao
    abstract fun commTrafficDao(): CommTrafficDao
    abstract fun userDao(): UserDao
    abstract fun fuelTransactionDao(): FuelTransactionDao
    abstract fun fuelLimitDao(): FuelLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "redimain_agenda_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
