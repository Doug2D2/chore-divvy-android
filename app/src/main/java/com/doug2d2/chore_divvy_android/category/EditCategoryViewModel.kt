package com.doug2d2.chore_divvy_android.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.Utils
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
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

    private val _editCategoryStatus = MutableLiveData<ApiStatus>()
    val editCategoryStatus: LiveData<ApiStatus>
        get() = _editCategoryStatus

    val categoryToEdit = MutableLiveData<Category>()
    val categoryName = MutableLiveData<String>()

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
            } catch (e: HttpException) {
                Timber.i("getCategoryById HttpException: " + e.message)
            } catch (e: Exception) {
                Timber.i("getCategoryById Exception: " + e.message)
            }
        }
    }

    // onSave is called when the Save button is clicked
    fun onSave() {
        // category name edit text value is stored in categoryName
        categoryToEdit.value?.categoryName = categoryName.value!!

        if (!categoryToEdit.value?.categoryName.isNullOrBlank()) {
            // Update Chore
            uiScope.launch {
                try {
                    _editCategoryStatus.value = ApiStatus.LOADING

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
}
