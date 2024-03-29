package com.app.progrec.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.progrec.beans.Task
import com.app.progrec.data.AppRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class CreateTaskViewModel(
    private val appRepository: AppRepository,
    private val classroomId: String,
    private val taskId: String?

) : BaseViewModel() {
    private val c = Calendar.getInstance()
    private val year = c.get(Calendar.YEAR)
    private val month = c.get(Calendar.MONTH)
    private val day = c.get(Calendar.DAY_OF_MONTH)

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val task = MutableLiveData<Task>()
    fun getTask() = task


    private val _showProgressBar = MutableLiveData<Boolean?>()
    val showProgressBar
        get() = _showProgressBar

    private val _navigateBackToTaskFragment = MutableLiveData<Boolean?>()
    val navigateBackToTaskFragment
        get() = _navigateBackToTaskFragment

    private val _pickDate = MutableLiveData<Boolean?>()
    val pickDate
        get() = _pickDate

    private val _stringLength = MutableLiveData<Int?>()
    val stringLength: LiveData<Int?>
        get() = _stringLength

    private val _descriptionStringLength = MutableLiveData<Int?>()
    val descriptionStringLength: LiveData<Int?>
        get() = _descriptionStringLength

    private val _showSnackBarHttpError = MutableLiveData<Int?>()
    val showSnackBarHttpError
        get() = _showSnackBarHttpError

    private val listenersList= mutableListOf<ListenerRegistration>()


    init {
        if (taskId != null) {
            setTaskListeners(taskId)
        }
    }

    private fun setTaskListeners(uid: String) {
        val db = appRepository.getFirestoreDB()
        val docRef = db.collection("tasks")
            .document(uid)
        val listener=docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.wtf("Listen failed $e")
            }
            if (snapshot != null && snapshot.exists()) {
                val taskFirestore =
                    snapshot.toObject(Task::class.java)
                Timber.wtf("classroom -> $taskFirestore")
                taskFirestore?.let {
                    task.value = it
                }
            } else {
                Timber.wtf("Current data: null")
            }
        }
        listenersList.add(listener)
    }


    fun onSavePressed(title: String, description: String, link: String, date: String) {
        when {
            title.length > 60 -> _stringLength.value = 1
            title.isEmpty() -> _stringLength.value = 2
            description.length > 100 -> _descriptionStringLength.value = 1
            description.isEmpty() -> _descriptionStringLength.value = 2
            else -> uiScope.launch {
                showProgressBar()
                withContext(Dispatchers.IO) {
                    if (taskId == null) {
                        if (appRepository.currentToken.value != null) {
                            try {
                                val request =
                                    appRepository.createTaskAsync(
                                        appRepository.currentToken.value!!,
                                        classroomId,
                                        title,
                                        description,
                                        link,
                                        date
                                    ).await()
                                if (request.isSuccessful) {
                                    val data = request.body()
                                    Timber.wtf(data.toString())
                                    data?.let {
                                        withContext(Dispatchers.Main) {
                                            _navigateBackToTaskFragment.value = true
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        showHttpErrorSnackBar400()
                                    }
                                    Timber.wtf("request failed :( ${request.code()}")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showHttpErrorSnackBarNetwork()
                                }
                                Timber.wtf("${e.message}${e.printStackTrace()}")
                            } finally {
                                withContext(Dispatchers.Main) {
                                    hideProgressBar()
                                }
                            }
                        }
                    } else {
                        val task = getTask().value

                        Timber.wtf(task.toString())
                        if (task != null) {
                            try {
                                task.title = title
                                task.description = description
                                task.endDate = date
                                task.referenceLink = link
                                val request =
                                    appRepository.updateTaskAsync(
                                        appRepository.currentToken.value!!,
                                        classroomId,
                                        task
                                    )
                                        .await()
                                if (request.isSuccessful) {
                                    val data = request.body()
                                    data?.let {
                                        withContext(Dispatchers.Main) {
                                            navigateBackToTaskFragment.value = true
                                        }
                                    }
                                } else {
                                    Timber.wtf("${request.code()}${request.raw()}")
                                }
                            } catch (e: Exception) {
                                Timber.wtf("oh no something went wrong!${e.printStackTrace()}${e.message}")
                            } finally {
                                withContext(Dispatchers.Main) {
                                    hideProgressBar()
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    fun onPressedDatePick() {
        _pickDate.value = true
    }

    fun onPickDateFinished() {
        _pickDate.value = null
    }

    override fun showProgressBar() {
        _showProgressBar.value = true
    }


    override fun hideProgressBar() {
        _showProgressBar.value = null
    }

    override fun navigate() {
        _navigateBackToTaskFragment.value = true
    }

    override fun onDoneNavigating() {
        _navigateBackToTaskFragment.value = null
    }

    override fun snackBarShown() {
        _stringLength.value = null
        _descriptionStringLength.value = null
    }


    override fun showHttpErrorSnackBar400() {
        _showSnackBarHttpError.value=1
    }

    override fun showHttpErrorSnackBarNetwork() {
        _showSnackBarHttpError.value=2
    }
    override fun hideHttpErrorSnackBar() {
        _showSnackBarHttpError.value=null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        uiScope.cancel()
        listenersList.forEach {
            it.remove()
        }
    }

}