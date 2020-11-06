package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.UserValidity
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

class EditCategoryViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val catDao = ChoreDivvyDatabase.getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val userDao = ChoreDivvyDatabase.getDatabase(application).userDao
    private val userRepository = UserRepository(userDao)

    private val _editCategoryStatus = MutableLiveData<ApiStatus>()
    val editCategoryStatus: LiveData<ApiStatus>
        get() = _editCategoryStatus

    private val _shouldSave = MutableLiveData<Boolean>()
    val shouldSave: LiveData<Boolean>
        get() = _shouldSave

    private val _addUserEditText = MutableLiveData<Boolean>()
    val addUserEditText: LiveData<Boolean>
        get() = _addUserEditText

    val categoryToEdit = MutableLiveData<Category>()
    val categoryName = MutableLiveData<String>()
    val userEmails = MutableLiveData<List<String>>()

    val saveButtonEnabled = MutableLiveData<Boolean>()

    val ctx = getApplication<Application>().applicationContext

    init {
        saveButtonEnabled.value = true

        getCategoryById()
    }

    // getCategoryById gets the category by id
    private fun getCategoryById() {
        val catId = Utils.getSelectedCategory(ctx)

        uiScope.launch {
            try {
                categoryToEdit.value = catRepository.getCategoryById(catId)
                categoryName.value = categoryToEdit.value?.categoryName

                getUserEmails(categoryToEdit.value?.userId!!)
            } catch (e: HttpException) {
                Timber.i("getCategoryById HttpException: " + e.message)
            } catch (e: Exception) {
                Timber.i("getCategoryById Exception: " + e.message)
            }
        }
    }

    // getUserEmails gets the user's emails by their id
    private fun getUserEmails(userIds: List<Int>) {
        uiScope.launch {
            try {
                // Don't include current user in list of userEmails to be edited or removed
                val currUserId = Utils.getUserId(ctx)
                val uIds = userIds.filter { id -> id != currUserId }

                userEmails.value = userRepository.getEmailsFromUserIds(uIds)
            } catch (e: HttpException) {
                Timber.i("getUserEmails HttpException: " + e.message)
            } catch (e: Exception) {
                Timber.i("getUserEmails Exception: " + e.message)
            }
        }
    }

    // onSave is called when the Save button is clicked
    fun onSave() {
        _editCategoryStatus.value = ApiStatus.LOADING
        _shouldSave.value = true
    }

    // doneSave is called when finished editing category
    fun doneSave() {
        _shouldSave.value = false
    }

    // getUserIds gets the user ids based on email addresses
    fun getUserIds(users: List<String>) {
        var userIds = mutableListOf<Int>(Utils.getUserId(ctx))

        // Get user ids from emails if length of users > 0
        if (users.isNotEmpty()) {
            try {
                uiScope.launch {
                    // Validate users
                    val allUsers = userRepository.getUsers()
                    when (Utils.validateUsers(users, allUsers)) {
                        UserValidity.VALID -> {
                            userIds.addAll(userRepository.getUserIdsFromEmails(users))
                            save(userIds.toSet().toList())
                        }
                        UserValidity.BAD_FORMAT -> {
                            Timber.i("One or more usernames are invalid format")
                            _editCategoryStatus.value = ApiStatus.USER_BAD_FORMAT_ERROR
                        }
                        UserValidity.DOESNT_EXIST -> {
                            Timber.i("One or more usernames do not exist")
                            _editCategoryStatus.value = ApiStatus.USER_DOESNT_EXIST_ERROR
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.i("getUserIds Exception: " + e.message)
                _editCategoryStatus.value = ApiStatus.OTHER_ERROR
            }
        } else {
            save(userIds)
        }
    }

    // save updates a category
    private fun save(userIds: List<Int>) {
        // category name edit text value is stored in categoryName and
        // userIds are stored in userIds
        categoryToEdit.value?.categoryName = categoryName.value!!
        categoryToEdit.value?.userId = userIds

        if (!categoryToEdit.value?.categoryName.isNullOrBlank()) {
            // Update Chore
            uiScope.launch {
                try {
                    catRepository.updateCategory(ctx, categoryToEdit.value!!)

                    _editCategoryStatus.value = ApiStatus.SUCCESS
                } catch (e: HttpException) {
                    Timber.i("editChore HttpException: " + e.message)
                    _editCategoryStatus.value = ApiStatus.CONNECTION_ERROR
                } catch (e: Exception) {
                    Timber.i("editChore Exception: " + e.message)
                    _editCategoryStatus.value = ApiStatus.OTHER_ERROR
                }
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
