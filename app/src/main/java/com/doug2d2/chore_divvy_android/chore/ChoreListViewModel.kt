package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.Chore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.sql.Date


enum class ChoreListStatus { LOADING, SUCCESS, UNAUTHORIZED, CONNECTION_ERROR, OTHER_ERROR }

class ChoreListViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

//    private val userRepository = UserRepository(getDatabase(application))

    private val _choreList = MutableLiveData<List<Chore>>()
    val choreList: LiveData<List<Chore>>
        get() = _choreList

    init {
        val d = Date(2020, 4, 1)
        val c1 = Chore(1, "Chore 1", "In Progress", d,
        1, 2, 3, 4, "Some notes here")
        val c2 = Chore(1, "Another Chore", "In Progress", d,
            1, 2, 3, 4, "Some more notes here")
        _choreList.value = listOf(c1, c2)
    }

}
