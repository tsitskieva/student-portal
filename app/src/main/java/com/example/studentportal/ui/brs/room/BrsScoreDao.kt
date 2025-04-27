package com.example.studentportal.ui.brs.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BrsScoreDao {
    @Query("SELECT * FROM brs_scores WHERE id = :id")
    suspend fun getById(id: Int): BrsScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: BrsScore)

    @Query("DELETE FROM brs_scores WHERE id = :id")
    suspend fun delete(id: Int)
}