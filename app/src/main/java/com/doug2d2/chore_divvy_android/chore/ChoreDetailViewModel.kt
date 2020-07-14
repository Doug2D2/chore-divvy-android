package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class ChoreDetailViewModel(application: Application): AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val dataSource = ChoreDivvyDatabase.getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(dataSource)

    val choreDetailView = MutableLiveData<Chore>()

    private val _deleteChoreStatus = MutableLiveData<ApiStatus>()
    val deleteChoreStatus: LiveData<ApiStatus>
        get() = _deleteChoreStatus

    private val _deleteChore = MutableLiveData<Boolean>()
    val deleteChore: LiveData<Boolean>
        get() = _deleteChore

    val ctx = getApplication<Application>().applicationContext

    fun onDelete() {
        _deleteChore.value = true
    }

    fun onDeleteCompleted() {
        _deleteChore.value = false
    }

    // deleteChore allows a user to delete a chore
    fun deleteChore(chore: Chore) {
        uiScope.launch {
            try {
                _deleteChoreStatus.value = ApiStatus.LOADING

                choreRepository.deleteChore(ctx, chore.id)

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
}
