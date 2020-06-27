package com.doug2d2.chore_divvy_android.repository

class DifficultyRepository {
    // getDifficulties returns a list of difficulties
    fun getDifficulties(): List<String> {
        return listOf<String>("Easy", "Medium", "Hard")
    }
}