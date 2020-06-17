package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

enum class ChoreListStatus { LOADING, SUCCESS, UNAUTHORIZED, CONNECTION_ERROR, OTHER_ERROR }

class ChoreListViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(dataSource)

    private val _choreListStatus = MutableLiveData<ChoreListStatus>()
    val choreListStatus: LiveData<ChoreListStatus>
        get() = _choreListStatus

    private var _choreList = MutableLiveData<List<Chore>>()
    val choreList: LiveData<List<Chore>>
        get() = _choreList

    private val _navigateToAddChore = MutableLiveData<Boolean>()
    val navigateToAddChore: LiveData<Boolean>
        get() = _navigateToAddChore

    init {
        getChores()
    }

    fun getChores() {
        uiScope.launch {
            try {
                Timber.i("Getting chores")
                _choreListStatus.value = ChoreListStatus.LOADING

                _choreList.value = choreRepository.getChores()

                _choreListStatus.value = ChoreListStatus.SUCCESS
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> _choreListStatus.value = ChoreListStatus.UNAUTHORIZED
                    else -> _choreListStatus.value = ChoreListStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.e(e)
                _choreListStatus.value = ChoreListStatus.CONNECTION_ERROR
            }
        }
    }

    fun updateChoreStatus(chore: Chore) {
        // Change status
        flipStatus(chore)

        GlobalScope.launch {
            try {
                // update chore and get chores
                choreRepository.updateChore(chore)
                getChores()
            } catch (e: HttpException) {
                Timber.i("updateChore HttpException: " + e.message)
                flipStatus(chore)

                // TODO: display error?
            } catch (e: Exception) {
                Timber.i("updateChore Exception: " + e.message)
                flipStatus(chore)

                // TODO: display error?
            }
        }
    }

    fun flipStatus(chore: Chore) {
        chore.status = when(chore.status) {
            "Completed" -> "In Progress"
            else -> "Completed"
        }
    }

    fun getChoreListItemIndex(chore: Chore): Int {
        var index = -1
        choreList.value?.mapIndexed { i, c ->
            if (chore.id == c.id) {
                index = i
            }
        }

        return index
    }

    fun onAddChore() {
        _navigateToAddChore.value = true
    }

    fun onNavigatedToAddChore() {
        _navigateToAddChore.value = false
    }
}
