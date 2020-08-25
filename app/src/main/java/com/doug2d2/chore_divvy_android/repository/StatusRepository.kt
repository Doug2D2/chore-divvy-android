package com.doug2d2.chore_divvy_android.repository

class StatusRepository {
    // getStatuses returns a list of statuses
    fun getStatuses(): List<String> {
        return listOf<String>("To Do", "In Progress", "Completed")
    }
}