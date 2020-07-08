package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.AddStatus
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

    private val _addCategoryStatus = MutableLiveData<AddStatus>()
    val addCategoryStatus: LiveData<AddStatus>
        get() = _addCategoryStatus

    val categoryName = MutableLiveData<String>()
    val addCategoryButtonEnabled = MutableLiveData<Boolean>()

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
                _addCategoryStatus.value = AddStatus.LOADING

                catRepository.addCategory(ctx, categoryToAdd)

                _addCategoryStatus.value = AddStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("addChore HttpException: " + e.message)
                _addCategoryStatus.value = AddStatus.CONNECTION_ERROR
            } catch (e: java.lang.Exception) {
                Timber.i("addChore Exception: " + e.message)
                _addCategoryStatus.value = AddStatus.OTHER_ERROR
            }
        }
    }
}
