package com.doug2d2.chore_divvy_android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
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

    private val _addCategoryStatus = MutableLiveData<ApiStatus>()
    val apiCategoryStatus: LiveData<ApiStatus>
        get() = _addCategoryStatus

    private val _deleteCategoryStatus = MutableLiveData<ApiStatus>()
    val deleteCategoryStatus: LiveData<ApiStatus>
        get() = _deleteCategoryStatus

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

    // deleteCategory allows a user to delete the current category
    fun deleteCategory() {
        val catId = Utils.getSelectedCategory(ctx)

        uiScope.launch {
            try {
                _deleteCategoryStatus.value = ApiStatus.LOADING

                categoryRepository.deleteCategory(ctx, catId)

                _deleteCategoryStatus.value = ApiStatus.SUCCESS
            } catch (e: HttpException) {
                Timber.i("deleteCategory HttpException: " + e.message)

                when(e.code()) {
                    401 -> _deleteCategoryStatus.value = ApiStatus.UNAUTHORIZED
                    else -> _deleteCategoryStatus.value = ApiStatus.OTHER_ERROR
                }
            } catch (e: Exception) {
                Timber.i("deleteCategory Exception: " + e.message)

                _deleteCategoryStatus.value = ApiStatus.CONNECTION_ERROR
            }
        }
    }

    // getCategoryNameById gets the name of a category by its id
    fun getCategoryNameById(categoryId: Int): String {
        val cats = _categories.value?.filter { cat ->
            cat.id == categoryId
        }

        return cats?.getOrNull(0)?.categoryName ?: ""
    }

    // getViewIdByCategoryId gets the view id associated with the category id
    fun getViewIdByCategoryId(categoryId: Int): Int {
        for ((k, v) in navigationViewMenuItems) {
            if (v.categoryId == categoryId) {
                return k
            }
        }

        return -99
    }
}

data class NavViewMenuItem(val categoryId: Int, val name: String)
