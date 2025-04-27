package com.example.studentportal.ui.brs.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BrsScore::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun brsScoreDao(): BrsScoreDao
}