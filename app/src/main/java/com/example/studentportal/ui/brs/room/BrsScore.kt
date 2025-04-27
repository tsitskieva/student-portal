package com.example.studentportal.ui.brs.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brs_scores")
data class BrsScore(
    @PrimaryKey val id: Int,
    val name: String,
    val currentScore: Int,
    val maxScore: Int,
    val lastUpdated: Long
)