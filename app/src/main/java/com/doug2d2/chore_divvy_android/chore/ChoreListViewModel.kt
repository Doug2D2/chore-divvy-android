package com.doug2d2.chore_divvy_android.chore

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import com.doug2d2.chore_divvy_android.user.LoginStatus
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber

enum class ChoreStatus { LOADING, SUCCESS, UNAUTHORIZED, CONNECTION_ERROR, OTHER_ERROR }

class ChoreListViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(dataSource)

    private val _choreListStatus = MutableLiveData<ChoreStatus>()
    val choreListStatus: LiveData<ChoreStatus>
        get() = _choreListStatus

    private val _updateChoreStatus = MutableLiveData<ChoreStatus>()
    val updateChoreStatus: LiveData<ChoreStatus>
        get() = _updateChoreStatus

    private val _deleteChoreStatus = MutableLiveData<ChoreStatus>()
    val deleteChoreStatus: LiveData<ChoreStatus>
        get() = _deleteChoreStatus

    private var _choreList = MutableLiveData<List<Chore>>()
    val choreList: LiveData<List<Chore>>
        get() = _choreList

    private val _navigateToAddChore = MutableLiveData<Boolean>()
    val navigateToAddChore: LiveData<Boolean>
        get() = _navigateToAddChore

    lateinit var choreToDelete: Chore

    init {
        getChores()
    }

    fun getChores() {
        uiScope.launch {
            try {
                Timber.i("Getting chores")
                _choreListStatus.value = ChoreStatus.LOADING

                _choreList.value = choreRepository.getChores()

                _choreListStatus.value = ChoreStatus.SUCCESS
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> _choreListStatus.value = ChoreStatus.UNAUTHORIZED
                    else -> _choreListStatus.value = ChoreStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.e(e)
                _choreListStatus.value = ChoreStatus.CONNECTION_ERROR
            }
        }
    }

    // updateChore allows a user to make a chore as completed or not completed
    fun updateChore(chore: Chore) {
        // Change status
        flipCompleted(chore)

        uiScope.launch {
            try {
                _updateChoreStatus.value = ChoreStatus.LOADING

                choreRepository.updateChore(chore)

                _updateChoreStatus.value = ChoreStatus.LOADING
            } catch (e: HttpException) {
                Timber.i("updateChore HttpException: " + e.message)
                flipCompleted(chore)

                when (e.code()) {
                    401 -> _updateChoreStatus.value = ChoreStatus.UNAUTHORIZED
                    else -> _updateChoreStatus.value = ChoreStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.i("updateChore Exception: " + e.message)
                flipCompleted(chore)

                _updateChoreStatus.value = ChoreStatus.CONNECTION_ERROR
            }
        }
    }

    // deleteChore allows a user to delete a chore
    fun deleteChore(chore: Chore) {
        uiScope.launch {
            try {
                _deleteChoreStatus.value = ChoreStatus.LOADING
                choreToDelete = chore
                choreRepository.deleteChore(choreToDelete.id)
                Timber.i("Chore deleted!")
                _choreList.value = _choreList.value?.filter { item ->
                    item.id != choreToDelete.id
                }
                _deleteChoreStatus.value = ChoreStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("deleteChore HttpException: " + e.message)

                when(e.code()) {
                    401 -> _deleteChoreStatus.value = ChoreStatus.UNAUTHORIZED
                    else -> _deleteChoreStatus.value = ChoreStatus.OTHER_ERROR
                }
                // TODO: display error?
            } catch (e: Exception) {
                Timber.i("deleteChore Exception: " + e.message)

                _deleteChoreStatus.value = ChoreStatus.CONNECTION_ERROR
                // TODO: display error?
            }
        }
    }

    fun flipCompleted(chore: Chore) {
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
