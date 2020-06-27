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

enum class ChoreStatus { LOADING, SUCCESS, UNAUTHORIZED, CONNECTION_ERROR, OTHER_ERROR }

class ChoreListViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(dataSource)

    private val _getChoresStatus = MutableLiveData<ChoreStatus>()
    val getChoresStatus: LiveData<ChoreStatus>
        get() = _getChoresStatus

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

    private val _navigateToEditChore = MutableLiveData<Boolean>()
    val navigateToEditChore: LiveData<Boolean>
        get() = _navigateToEditChore

    lateinit var choreToDelete: Chore
    lateinit var choreToUpdate: Chore
    lateinit var choreToEdit: Chore

    init {
        getChores()
    }

    // getChores gets all chores from the API and updates the local DB with them
    private fun getChores() {
        uiScope.launch {
            try {
                Timber.i("Getting chores")
                _getChoresStatus.value = ChoreStatus.LOADING

                _choreList.value = choreRepository.getChores()

                _getChoresStatus.value = ChoreStatus.SUCCESS
            } catch (e: HttpException) {
                when (e.code()) {
                    401 -> _getChoresStatus.value = ChoreStatus.UNAUTHORIZED
                    else -> _getChoresStatus.value = ChoreStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.e(e)
                _getChoresStatus.value = ChoreStatus.CONNECTION_ERROR
            }
        }
    }

    // updateChore allows a user to mark a chore as completed or not completed
    fun updateChore(chore: Chore) {
        // Change status
        flipCompleted(chore)

        uiScope.launch {
            try {
                _updateChoreStatus.value = ChoreStatus.LOADING

                choreToUpdate = chore
                choreRepository.updateChore(chore)

                _updateChoreStatus.value = ChoreStatus.SUCCESS
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
                // Remove deleted chore from _choreList
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
            } catch (e: Exception) {
                Timber.i("deleteChore Exception: " + e.message)

                _deleteChoreStatus.value = ChoreStatus.CONNECTION_ERROR
            }
        }
    }

    // flipCompleted flips the chore status from Completed to In Progress
    // and vise versa
    private fun flipCompleted(chore: Chore) {
        chore.status = when(chore.status) {
            "Completed" -> "In Progress"
            else -> "Completed"
        }
    }

    // getChoreListItemIndex gets the index of the chore in the choreList
    fun getChoreListItemIndex(chore: Chore): Int {
        var index = -1
        choreList.value?.mapIndexed { i, c ->
            if (chore.id == c.id) {
                index = i
            }
        }

        return index
    }

    // onAddChore navigates to the add chore fragment
    fun onAddChore() {
        _navigateToAddChore.value = true
    }

    // onNavigatedToAddChore is called after navigating to the add chore fragment
    fun onNavigatedToAddChore() {
        _navigateToAddChore.value = false
    }

    // onEditChore navigates to the add chore fragment
    fun onEditChore(chore: Chore) {
        choreToEdit = chore
        _navigateToEditChore.value = true
    }

    // onNavigatedToEditChore is called after navigating to the add chore fragment
    fun onNavigatedToEditChore() {
        _navigateToEditChore.value = false
    }
}
