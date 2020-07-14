package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import kotlinx.android.synthetic.main.activity_main.view.*
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

    private var _choreList = MutableLiveData<List<Chore>>()
    val choreList: LiveData<List<Chore>>
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

    lateinit var choreToDelete: Chore
    lateinit var choreToUpdate: Chore
    lateinit var choreToEdit: Chore
    lateinit var choreDetailView: Chore

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
    fun updateChore(chore: Chore) {
        // Change status
        flipCompleted(chore)

        uiScope.launch {
            try {
                _updateChoreStatus.value = ApiStatus.LOADING

                choreToUpdate = chore
                choreRepository.updateChore(ctx, chore)

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
    fun deleteChore(chore: Chore) {
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

    // onDetailView navigates to the chore detail fragment
    fun onDetailView(chore: Chore) {
        choreDetailView = chore
        _navigateToDetailView.value = true
    }

    // onNavigatedToDetailView is called after navigating to the chore detail fragment
    fun onNavigatedToDetailView() {
        _navigateToDetailView.value = false
    }
}
