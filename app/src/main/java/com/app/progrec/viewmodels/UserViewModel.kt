package com.app.progrec.viewmodels

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import com.app.progrec.R
import com.app.progrec.beans.Classroom
import com.app.progrec.beans.User
import com.app.progrec.data.AppRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import timber.log.Timber

class UserViewModel(private val appRepository: AppRepository, private val classroomId: String) :
    BaseViewModel() {


    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _isAdmin = appRepository.isAdmin
    val isAdmin
        get() = _isAdmin


    private val adapterList = hashMapOf<String, User>()
    val users = MutableLiveData<List<User>>()

    private val classroom = MutableLiveData<Classroom>()
    fun getClassroom() = classroom

    private val _removeUser = MutableLiveData<Pair<String, String>>()
    val removeUser
        get() = _removeUser

    private val _transfer = MutableLiveData<Pair<String, String>>()
    val transfer
        get() = _transfer

    private val _transferSuccessful = MutableLiveData<Boolean?>()
    val transferSuccessful
        get() = _transferSuccessful

    private val _removedUserSnackBar = MutableLiveData<Boolean?>()
    val removedUserSnackBar
        get() = _removedUserSnackBar

    private val _showProgressBar = MutableLiveData<Boolean?>()
    val showProgressBar
        get() = _showProgressBar

    private val _showSnackBarClassroom = MutableLiveData<Boolean?>()
    val showSnackBarClassroom
        get() = _showSnackBarClassroom

    private val _showSnackBarRefresh = MutableLiveData<Boolean?>()
    val showSnackBarRefresh
        get() = _showSnackBarRefresh

    private val _navigateBackToClassroomFragment = MutableLiveData<Boolean?>()
    val navigateBackToClassroomFragment
        get() = _navigateBackToClassroomFragment

    private val _showSnackBarHttpError = MutableLiveData<Int?>()
    val showSnackBarHttpError
        get() = _showSnackBarHttpError

    private val listenersList= mutableListOf<ListenerRegistration>()

    init {
        setClassroomListener(classroomId)
        loadUsers()
    }

    fun loadUsers() {
        uiScope.launch {
            showProgressBar()
            withContext(Dispatchers.IO) {
                if (appRepository.currentToken.value != null) {
                    try {
                        Timber.wtf(classroomId)
                        val response = appRepository.getUsersInClassroomAsync(
                            appRepository.currentToken.value!!,
                            classroomId
                        ).await()
                        if (response.isSuccessful) {
                            val data = response.body()
                            data?.forEach {
                                setUsersListeners(it.key)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                showHttpErrorSnackBar400()
                            }
                            Timber.wtf("Something went wrong ${response.code()} ${response.errorBody().toString()}")
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
            }
        }
    }

    private fun setClassroomListener(uid: String) {
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
                        classroom.value = it
                    } else {
                        if (appRepository.isAdmin.value == false) {
                            showSnackBarClassroomDeleted()
                            onClassroomDeleted()
                        }
                    }
                }
            } else {
                Timber.wtf("Current data: null")
            }
        }
        listenersList.add(listener)
    }

    private fun setUsersListeners(uid: String) {
        val db = appRepository.getFirestoreDB()
        val docRef = db.collection("users")
            .document(uid)
        val listener=docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.wtf("Listen failed $e")
            }
            if (snapshot != null && snapshot.exists()) {
                val userFirestore =
                    snapshot.toObject(User::class.java)
                Timber.wtf("user -> $userFirestore")
                userFirestore?.let {
                    adapterList[it.uid] = it
                    users.value = adapterList.values.toList()
                }
            } else {
                Timber.wtf("Current data: null")
            }
        }
        listenersList.add(listener)
    }


    fun onUserClicked(user: User, context: Context, view: View) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.users_menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.remove_user -> {
                    showRemoveUserDialog(user.fullName, user.uid)
                }
                R.id.transfer -> {
                    showTransferDialog(user.fullName, user.uid)
                }
            }
            true
        }
        popup.show()
    }


    fun transferClassroom(userUid: String) {
        uiScope.launch {
            showProgressBar()
            withContext(Dispatchers.IO) {
                if (appRepository.currentToken.value != null) {
                    try {
                        val request = appRepository.transferClassroomAsync(
                            appRepository.currentToken.value!!,
                            classroomId,
                            userUid
                        ).await()
                        if (request.isSuccessful) {
                            val data = request.body()
                            data?.let{
                                loadUsers()
                            }
                            withContext(Dispatchers.Main) {
                                hideProgressBar()
                                showTransferSuccessful()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                showHttpErrorSnackBar400()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showHttpErrorSnackBarNetwork()
                        }
                        Timber.wtf("${e.printStackTrace()}${e.message}")
                    }
                }
            }

        }
    }

    fun removeUser(userUid: String) {
        uiScope.launch {
            showProgressBar()
            withContext(Dispatchers.IO) {
                if (appRepository.currentToken.value != null) {
                    try {
                        val request = appRepository.removeUserAsync(
                            appRepository.currentToken.value!!,
                            classroomId,
                            userUid
                        ).await()
                        if (request.isSuccessful) {
                            val data = request.body()
                           data?.let{
                               withContext(Dispatchers.Main) {
                                   adapterList.remove(userUid)
                                   users.value=adapterList.values.toList()
                                   showRemovedUser()
                               }
                               loadUsers()
                           }
                        } else {
                            withContext(Dispatchers.Main) {
                                showHttpErrorSnackBar400()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showHttpErrorSnackBarNetwork()
                        }
                        Timber.wtf("${e.printStackTrace()}${e.cause}")
                    } finally {
                        withContext(Dispatchers.Main) {
                            hideProgressBar()
                        }
                    }
                }
            }
        }
    }




    override fun showProgressBar() {
        super.showProgressBar()
        _showProgressBar.value = true
    }

    override fun hideProgressBar() {
        super.hideProgressBar()
        _showProgressBar.value = null
    }

    private fun showRemoveUserDialog(userName: String, userUid: String) {
        _removeUser.value = userName to userUid
    }

    fun hideRemoveUserDialog() {
        _removeUser.value = null
    }

    private fun showTransferDialog(userName: String, userUid: String) {
        _transfer.value = userName to userUid
    }

    fun hideTransferDialog() {
        _transfer.value = null
    }

    private fun showTransferSuccessful() {
        _transferSuccessful.value = true
    }

    fun hideTransferSuccessful() {
        _transferSuccessful.value = null
    }

    private fun showRemovedUser() {
        _removedUserSnackBar.value = true
    }

    fun hideRemoveUserSnackBar() {
        _removedUserSnackBar.value = null
    }

    fun showSnackBarClassroomDeleted() {
        _showSnackBarClassroom.value = true
    }

    fun hideSnackBarClassroomDeleted() {
        _showSnackBarClassroom.value = null
    }


    fun showSnackBarRefresh() {
        _showSnackBarRefresh.value = true
    }

    fun hideRefreshSnackBar() {
        _showSnackBarRefresh.value = null
    }

    private fun onClassroomDeleted() {
        _navigateBackToClassroomFragment.value = true
    }

    fun doneNavigateToClassroomFragment() {
        _navigateBackToClassroomFragment.value = null
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