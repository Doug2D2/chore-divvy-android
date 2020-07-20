package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.network.AddCategoryRequest
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import retrofit2.HttpException
import timber.log.Timber
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.repository.UserRepository
import kotlinx.coroutines.*
import java.lang.Exception

class AddCategoryViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val catDao = ChoreDivvyDatabase.getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val userDao = ChoreDivvyDatabase.getDatabase(application).userDao
    private val userRepository = UserRepository(userDao)

    private val _addCategoryStatus = MutableLiveData<ApiStatus>()
    val apiCategoryStatus: LiveData<ApiStatus>
        get() = _addCategoryStatus

    private val _shouldAddCategory = MutableLiveData<Boolean>()
    val shouldAddCategory: LiveData<Boolean>
        get() = _shouldAddCategory

    private val _addUserEditText = MutableLiveData<Boolean>()
    val addUserEditText: LiveData<Boolean>
        get() = _addUserEditText

    val categoryName = MutableLiveData<String>()
    val addCategoryButtonEnabled = MutableLiveData<Boolean>()
    var newCategoryId = -1

    val ctx = getApplication<Application>().applicationContext

    init {
        addCategoryButtonEnabled.value = false
    }

    // onAddCategory is called when the Add button is clicked
    fun onAddCategory() {
        _addCategoryStatus.value = ApiStatus.LOADING
        _shouldAddCategory.value = true
    }

    // doneAddCategory is called when finished adding category
    fun doneAddCategory() {
        _shouldAddCategory.value = false
    }

    // getUserIds gets the user ids based on email addresses
    fun getUserIds(users: List<String>) {
        var userIds = mutableListOf<Int>(Utils.getUserId(ctx))

        // Get user ids from emails if length of users > 0
        if (users.isNotEmpty()) {
            try {
                uiScope.launch {
                    userIds.addAll(userRepository.getUserIdsFromEmails(users))
                    addCategory(userIds.toSet().toList())
                }
            } catch (e: Exception) {
                Timber.i("getUserIds Exception: " + e.message)
                _addCategoryStatus.value = ApiStatus.OTHER_ERROR
            }
        } else {
            addCategory(userIds)
        }
    }

    // addCategory creates a new category
    private fun addCategory(userIds: List<Int>) {
        val categoryToAdd = AddCategoryRequest(
            categoryName = categoryName.value!!,
            userIds = userIds
        )

        // Add Category
        uiScope.launch {
            try {
                newCategoryId = catRepository.addCategory(ctx, categoryToAdd)

                _addCategoryStatus.value = ApiStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("addChore HttpException: " + e.message)
                _addCategoryStatus.value = ApiStatus.CONNECTION_ERROR
            } catch (e: java.lang.Exception) {
                Timber.i("addChore Exception: " + e.message)
                _addCategoryStatus.value = ApiStatus.OTHER_ERROR
            }
        }
    }

    // onAddUserEditText is called when the Add User button is clicked
    fun onAddUserEditText() {
        _addUserEditText.value = true
    }

    // doneAddUserEditText is called when finished adding user edit text
    fun doneAddUserEditText() {
        _addUserEditText.value = false
    }
}
