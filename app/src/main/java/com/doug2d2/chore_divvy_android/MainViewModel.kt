package com.doug2d2.chore_divvy_android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.network.AddCategoryRequest
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MainViewModel (application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = ChoreDivvyDatabase.getDatabase(application).categoryDao
    private val categoryRepository = CategoryRepository(dataSource)

    private var _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>>
        get() = _categories

    private val _addCategoryStatus = MutableLiveData<AddStatus>()
    val addCategoryStatus: LiveData<AddStatus>
        get() = _addCategoryStatus

    var navigationViewMenuItems: MutableMap<Int, NavViewMenuItem> = HashMap()

    val ctx = getApplication<Application>().applicationContext

    init {
        getCategories()
    }

    // getCategories gets all categories from the API and updates the local DB with them
    fun getCategories() {
        uiScope.launch {
            try {
                _categories.value = categoryRepository.getCategories(ctx)
            } catch (e: HttpException) {
                Timber.i("getCategories Http Exception: " + e.message)
            } catch (e: Exception) {
                Timber.i("getCategories Exception: " + e.message)
            }
        }
    }

    // onAddCategory is called when the Add Category menu item is clicked
    fun onAddCategory() {
        val userId = Utils.getUserId(ctx)
        val categoryToAdd = AddCategoryRequest(
            categoryName = "NEW",
            userIds = listOf(userId)
        )

        // Add Category
        uiScope.launch {
            try {
                _addCategoryStatus.value = AddStatus.LOADING

                categoryRepository.addCategory(ctx, categoryToAdd)

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

data class NavViewMenuItem(val categoryId: Int, val name: String)
