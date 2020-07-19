package com.doug2d2.chore_divvy_android.category

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.network.AddCategoryRequest
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class AddCategoryViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val catDao = ChoreDivvyDatabase.getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val _addCategoryStatus = MutableLiveData<ApiStatus>()
    val apiCategoryStatus: LiveData<ApiStatus>
        get() = _addCategoryStatus

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
        val userId = Utils.getUserId(ctx)
        val categoryToAdd = AddCategoryRequest(
            categoryName = categoryName.value!!,
            userIds = listOf(userId)
        )

        // Add Category
        uiScope.launch {
            try {
                _addCategoryStatus.value = ApiStatus.LOADING

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
