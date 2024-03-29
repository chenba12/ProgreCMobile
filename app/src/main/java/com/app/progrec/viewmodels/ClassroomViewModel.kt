package com.app.progrec.viewmodels

import androidx.lifecycle.MutableLiveData
import com.app.progrec.beans.Classroom
import com.app.progrec.data.AppRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import timber.log.Timber

class ClassroomViewModel constructor(
    private val appRepository: AppRepository
) :
    BaseViewModel() {


    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _isAdmin = MutableLiveData<Boolean?>()
    val isAdmin
        get() = _isAdmin

    private val adapterList = hashMapOf<String, Classroom>()
    val classrooms = MutableLiveData<List<Classroom>>()

    private var _isEmpty = MutableLiveData<Boolean?>()
    val isEmpty
        get() = _isEmpty


    val user = appRepository.getUser()

    private val _navigateToTaskFragment = MutableLiveData<String>()
    val navigateToTaskFragment
        get() = _navigateToTaskFragment

    private val _navigateToCreateClassroomFragment = MutableLiveData<Boolean?>()
    val navigateToCreateClassroomFragment
        get() = _navigateToCreateClassroomFragment

    private val _showProgressBar = MutableLiveData<Boolean?>()
    val showProgressBar
        get() = _showProgressBar

    private val _showSnackBarRefresh = MutableLiveData<Boolean?>()
    val showSnackBarRefresh
        get() = _showSnackBarRefresh

    private val _showSnackBarHttpError = MutableLiveData<Int?>()
    val showSnackBarHttpError
        get() = _showSnackBarHttpError

    private val listenersList= mutableListOf<ListenerRegistration>()


    init {
        getCurrentUser()
    }

    fun fetchClassrooms() {
        listenersList.clear()
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val request =
                        appRepository.getClassroomsAsync(appRepository.currentToken.value!!).await()
                    if (request.isSuccessful) {
                        val classroomsData = request.body()
                        classroomsData?.let {
                            withContext(Dispatchers.Main) {
                                _isEmpty.value = false
                            }
                            classroomsData.forEach { classroomEntry ->
                                setListeners(classroomEntry.key)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _isEmpty.value = true
                            Timber.wtf("request failed ${request.code()}${request.errorBody()}")
                            showHttpErrorSnackBar400()
                        }
                    }
                } catch (e: Exception) {
                    Timber.wtf("${e.message}${e.message}${e.stackTrace}")
                    withContext(Dispatchers.Main) {
                        showHttpErrorSnackBarNetwork()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        hideProgressBar()
                    }
                }
            }
        }

    }

    private fun getCurrentUser() {
        val auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (appRepository.currentToken.value == null) {
            if (currentUser?.email.equals("hedsean@gmail.com")) {
                _isAdmin.value = appRepository.isAdmin()
            } else {
                _isAdmin.value = appRepository.notAdmin()
            }
            currentUser!!.getIdToken(true).addOnCompleteListener {
                if (it.isSuccessful) {
                    Timber.wtf(it.result?.token)
                    uiScope.launch {
                        showProgressBar()
                        appRepository.setToken(it.result?.token!!)
                        val token = appRepository.currentToken.value
                        withContext(Dispatchers.IO) {
                            if (token != null) {
                                try {
                                    val request =
                                        appRepository.getCurrentUserAsync(token).await()
                                    if (request.isSuccessful) {
                                        val data = request.body()
                                        data?.let { user ->
                                            withContext(Dispatchers.Main) {
                                                appRepository.setCurrentUser(user)
                                            }
                                            fetchClassrooms()
                                        }
                                    } else {
                                        Timber.wtf("the error code is ${request.code()}")
                                        withContext(Dispatchers.Main) {
                                            showHttpErrorSnackBarServer()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        showHttpErrorSnackBarNetwork()
                                    }
                                    Timber.e(" catch clause -> ${e.printStackTrace()}${e.message}")
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
        } else {
            fetchClassrooms()
        }
    }

    private fun setListeners(uid: String) {
        val db = appRepository.getFirestoreDB()
        val docRef = db.collection("classrooms")
            .document(uid)

        val listener=docRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                Timber.wtf("Listen failed $e")
            }
            if (snapshot != null && snapshot.exists()) {
                val classroomFirestore =
                    snapshot.toObject(Classroom::class.java)
                Timber.wtf("classroom -> $classroomFirestore")
                classroomFirestore?.let {
                    if (!it.archived) {
                        adapterList[it.uid] = it
                        classrooms.value = adapterList.values.toList()
                    }
                }
            } else {
                Timber.wtf("Current data: null")
            }

        }
        listenersList.add(listener)
    }

    fun onClassroomClicked(uid: String) {
        _navigateToTaskFragment.value = uid
    }

    fun doneNavigateToTaskFragment() {
        _navigateToTaskFragment.value = null
    }

    fun navigateToCreateClassroomFragment() {
        _navigateToCreateClassroomFragment.value = true
    }

    fun doneNavigateToCreateClassroomFragment() {
        _navigateToCreateClassroomFragment.value = null
    }

    override fun showProgressBar() {
        _showProgressBar.value = true
    }

    override fun hideProgressBar() {
        _showProgressBar.value = false
    }

    fun showSnackBarRefresh() {
        _showSnackBarRefresh.value = true
    }

    fun hideRefreshSnackBar() {
        _showSnackBarRefresh.value = null
    }


    override fun showHttpErrorSnackBar400() {
        _showSnackBarHttpError.value = 1
    }

    override fun showHttpErrorSnackBarNetwork() {
        _showSnackBarHttpError.value = 2
    }

    override fun showHttpErrorSnackBarServer() {
        _showSnackBarHttpError.value=3
    }

    override fun hideHttpErrorSnackBar() {
        _showSnackBarHttpError.value = null
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