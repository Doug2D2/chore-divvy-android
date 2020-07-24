package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.database.FullChore
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import com.squareup.moshi.Json
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber

class ChoreListViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(dataSource)

    val catDataSource = ChoreDivvyDatabase.getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDataSource)

    private val _getChoresStatus = MutableLiveData<ApiStatus>()
    val getChoresStatus: LiveData<ApiStatus>
        get() = _getChoresStatus

    private val _updateChoreStatus = MutableLiveData<ApiStatus>()
    val updateChoreStatus: LiveData<ApiStatus>
        get() = _updateChoreStatus

    private val _deleteChoreStatus = MutableLiveData<ApiStatus>()
    val deleteChoreStatus: LiveData<ApiStatus>
        get() = _deleteChoreStatus

    private val _assignChoreStatus = MutableLiveData<ApiStatus>()
    val assignChoreStatus: LiveData<ApiStatus>
        get() = _assignChoreStatus

    private val _unassignChoreStatus = MutableLiveData<ApiStatus>()
    val unassignChoreStatus: LiveData<ApiStatus>
        get() = _unassignChoreStatus

    private var _choreList = MutableLiveData<List<FullChore>>()
    val choreList: LiveData<List<FullChore>>
        get() = _choreList

    private val _navigateToAddChore = MutableLiveData<Boolean>()
    val navigateToAddChore: LiveData<Boolean>
        get() = _navigateToAddChore

    private val _navigateToEditChore = MutableLiveData<Boolean>()
    val navigateToEditChore: LiveData<Boolean>
        get() = _navigateToEditChore

    private val _navigateToDetailView = MutableLiveData<Boolean>()
    val navigateToDetailView: LiveData<Boolean>
        get() = _navigateToDetailView

    lateinit var choreToDelete: FullChore
    lateinit var choreToUpdate: FullChore
    lateinit var choreToEdit: FullChore
    lateinit var choreDetailView: FullChore

    var shouldGetCategory = true

    val ctx = getApplication<Application>().applicationContext

    init {
        getChores()
    }

    // getChores gets all chores from the API and updates the local DB with them
    private fun getChores() {
        if (!Utils.isSelectedCategorySet(ctx) && shouldGetCategory) {
            setDefaultSelectedCategory()
            shouldGetCategory = false
        } else {
            uiScope.launch {
                try {
                    Timber.i("Getting chores")
                    _getChoresStatus.value = ApiStatus.LOADING

                    _choreList.value = choreRepository.getChores(ctx)

                    _getChoresStatus.value = ApiStatus.SUCCESS
                } catch (e: HttpException) {
                    Timber.i("getChores Http Exception: " + e.message)

                    when (e.code()) {
                        401 -> _getChoresStatus.value = ApiStatus.UNAUTHORIZED
                        else -> _getChoresStatus.value = ApiStatus.OTHER_ERROR
                    }
                } catch (e: Exception) {
                    Timber.i("getChores Exception: " + e.message)

                    _getChoresStatus.value = ApiStatus.CONNECTION_ERROR
                }
            }
        }
    }

    // setDefaultSelectedCategory gets all categories from the API and updates the local DB with them
    // then sets the selected category to the first category returned
    private fun setDefaultSelectedCategory() {
        uiScope.launch {
            try {
                // Get categories
                val cats = catRepository.getCategories(ctx)

                // Set selected category
                if (cats.isNotEmpty()) {
                    Utils.setSelectedCategory(ctx, cats[0].id)

                    // Get chores with selected category set
                    getChores()
                }
            } catch (e: HttpException) {
                Timber.i("getCategories Http Exception: " + e.message)
            } catch (e: Exception) {
                Timber.i("getCategories Exception: " + e.message)
            }
        }
    }

    // updateChore allows a user to mark a chore as completed or not completed
    fun updateChore(chore: FullChore) {
        // Change status
        flipCompleted(chore)

        uiScope.launch {
            try {
                _updateChoreStatus.value = ApiStatus.LOADING

                choreToUpdate = chore

                // Create Chore object to update database with
                val c = Chore(id = chore.id, choreName = chore.choreName,
                    status = chore.status, dateComplete = chore.dateComplete,
                    frequencyId = chore.frequencyId, categoryId = chore.categoryId,
                    assigneeId = chore.assigneeId, difficulty = chore.difficulty,
                    notes = chore.notes, createdAt = chore.createdAt,
                    updatedAt = chore.updatedAt)
                choreRepository.updateChore(ctx, c)

                _updateChoreStatus.value = ApiStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("updateChore HttpException: " + e.message)
                flipCompleted(chore)

                when (e.code()) {
                    401 -> _updateChoreStatus.value = ApiStatus.UNAUTHORIZED
                    else -> _updateChoreStatus.value = ApiStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.i("updateChore Exception: " + e.message)
                flipCompleted(chore)

                _updateChoreStatus.value = ApiStatus.CONNECTION_ERROR
            }
        }
    }

    // deleteChore allows a user to delete a chore
    fun deleteChore(chore: FullChore) {
        uiScope.launch {
            try {
                _deleteChoreStatus.value = ApiStatus.LOADING

                choreToDelete = chore
                choreRepository.deleteChore(ctx, choreToDelete.id)

                // Remove deleted chore from _choreList
                _choreList.value = _choreList.value?.filter { item ->
                    item.id != choreToDelete.id
                }

                _deleteChoreStatus.value = ApiStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("deleteChore HttpException: " + e.message)

                when(e.code()) {
                    401 -> _deleteChoreStatus.value = ApiStatus.UNAUTHORIZED
                    else -> _deleteChoreStatus.value = ApiStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.i("deleteChore Exception: " + e.message)

                _deleteChoreStatus.value = ApiStatus.CONNECTION_ERROR
            }
        }
    }

    // flipCompleted flips the chore status from Completed to In Progress
    // and vise versa
    private fun flipCompleted(chore: FullChore) {
        chore.status = when(chore.status) {
            "Completed" -> "In Progress"
            else -> "Completed"
        }
    }

    // getChoreListItemIndex gets the index of the chore in the choreList
    fun getChoreListItemIndex(chore: FullChore): Int {
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
    fun onEditChore(chore: FullChore) {
        choreToEdit = chore
        _navigateToEditChore.value = true
    }

    // onNavigatedToEditChore is called after navigating to the add chore fragment
    fun onNavigatedToEditChore() {
        _navigateToEditChore.value = false
    }

    // onDetailView navigates to the chore detail fragment
    fun onDetailView(chore: FullChore) {
        choreDetailView = chore
        _navigateToDetailView.value = true
    }

    // onNavigatedToDetailView is called after navigating to the chore detail fragment
    fun onNavigatedToDetailView() {
        _navigateToDetailView.value = false
    }

    // onAssignToMe assigns a chore to a user
    fun onAssignToMe(chore: FullChore, userId: Int) {
        uiScope.launch {
            try {
                choreToUpdate = chore

                // Create Chore object to update database with
                val c = Chore(id = chore.id, choreName = chore.choreName,
                    status = chore.status, dateComplete = chore.dateComplete,
                    frequencyId = chore.frequencyId, categoryId = chore.categoryId,
                    assigneeId = userId, difficulty = chore.difficulty,
                    notes = chore.notes, createdAt = chore.createdAt,
                    updatedAt = chore.updatedAt)

                choreRepository.updateChore(ctx, c)

                // Update _choreList with new assigneeId
                _choreList.value = _choreList.value?.map { item ->
                    if (item.id == chore.id) {
                        item.assigneeId = userId
                    }
                    item
                }

                _assignChoreStatus.value = ApiStatus.SUCCESS
            } catch (e: java.lang.Exception) {
                Timber.i("onAssignToMe Exception: " + e.message)
                _assignChoreStatus.value = ApiStatus.OTHER_ERROR
            }
        }
    }

    // onUnassign unassigns a user from a chore
    fun onUnassign(chore: FullChore) {
        uiScope.launch {
            try {
                choreToUpdate = chore

                // Create Chore object to update database with
                // -1 should be used as assigneeId to represent unassigned
                val c = Chore(id = chore.id, choreName = chore.choreName,
                    status = chore.status, dateComplete = chore.dateComplete,
                    frequencyId = chore.frequencyId, categoryId = chore.categoryId,
                    assigneeId = -1, difficulty = chore.difficulty,
                    notes = chore.notes, createdAt = chore.createdAt,
                    updatedAt = chore.updatedAt)

                choreRepository.updateChore(ctx, c)

                // Update _choreList with new null assigneeId
                _choreList.value = _choreList.value?.map { item ->
                    if (item.id == chore.id) {
                        item.assigneeId = null
                        item.firstName = ""
                        item.lastName = ""
                        item.username = ""
                    }
                    item
                }

                _unassignChoreStatus.value = ApiStatus.SUCCESS
            } catch (e: java.lang.Exception) {
                Timber.i("onUnassign Exception: " + e.message)
                _unassignChoreStatus.value = ApiStatus.OTHER_ERROR
            }
        }
    }
}
