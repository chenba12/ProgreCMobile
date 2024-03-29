package com.app.progrec.utils

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.app.progrec.R
import com.app.progrec.beans.*
import timber.log.Timber
import java.util.*
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri


@BindingAdapter("setFullName")
fun TextView.setFullName(user: User?) {
    user?.let {
        text = user.fullName
    }
}

@BindingAdapter("setEmail")
fun TextView.setEmail(user: User?) {
    user?.let {
        text = user.email
    }
}

//TODO change time zones
@BindingAdapter("setLastLoggedIn")
fun TextView.setLastLoggedIn(user: User?) {
    user?.let {

        text = context.getString(R.string.last_login_date, user.signedIn)
    }
}

@BindingAdapter("setProfilePic")
fun ImageView.setProfilePic(user: User?) {
    user?.let {
        Glide.with(context)
            .load(user.profilePictureUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.circleCropTransform())
            .apply(RequestOptions.overrideOf(250, 250))
            .into(this)
    }
}

@BindingAdapter("classroomOwner")
fun TextView.setClassroomOwner(item: Classroom?) {
    item?.let {
        text = item.owner
    }
}


@BindingAdapter("classroomName")
fun TextView.setClassroomName(item: Classroom?) {
    item?.let {
        text = item.name
    }
}

@BindingAdapter("setClassroomDescription")
fun TextView.setClassroomDescription(item: Classroom?) {
    item?.let {
        text = item.description
    }
}

@BindingAdapter("setClassroomNumberOfTasks")
fun TextView.setClassroomNumberOfTasks(item: Classroom?) {
    item?.let {
        text = context.getString(R.string.number_of_tasks, item.numberOfTasks)
    }
}

@BindingAdapter("taskTitle")
fun TextView.setTaskTitle(item: Task?) {
    item?.let {
        text = item.title
    }
}

@BindingAdapter("setCurrentDate")
fun TextView.setCurrentDate(a: Int) {
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)
    text = context?.getString(R.string.current_date, day, month + 1, year)

}

@BindingAdapter("taskDueDate")
fun TextView.setTaskDueDate(item: Task?) {
    item?.let {


        text = context.getString(R.string.due_by, it.endDate)
    }
}

@BindingAdapter("setTaskStatus")
fun ImageView.setTaskStatus(item: Task?) {
    item?.let {
        if (!it.completed) {
            this.setImageResource(R.drawable.ic_lock_open_24px)
        } else {
            this.setImageResource(R.drawable.ic_lock_24px)
            this.isClickable=false
        }
    }
}

@BindingAdapter("taskDescription")
fun TextView.setTaskDescription(item: Task?) {
    item?.let {
        text = item.description
    }
}


@BindingAdapter("setLinks")
fun TextView.setLinks(item: Task?) {
    item?.let { task ->
        var url = task.referenceLink
        this.setOnClickListener {
            if (!url!!.startsWith("http://") && !url!!.startsWith("https://"))
                url = "http://$url"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            val packageManager = context.packageManager

            val activities: List<ResolveInfo> = packageManager.queryIntentActivities(
                browserIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (browserIntent.resolveActivity(packageManager) != null) {
                startActivity(context, browserIntent, null)
            }
        }
        text = task.referenceLink
        this.setTextColor(Color.parseColor("#0000FF"))
    }
}

@BindingAdapter("exerciseText")
fun TextView.setExerciseText(item: Exercise?) {
    item?.let {
        Timber.wtf("setExerciseText triggered with exercise -->> ${it.uid} and ${it.exerciseTitle}")
        text = item.exerciseTitle
    }
}

@BindingAdapter("userFinishedEmail")
fun TextView.setUserFinishedEmail(item: FinishedUser?) {
    item?.let {
        text = item.email
    }
}

@BindingAdapter("userFinishedTimestamp")
fun TextView.setUserFinishedTimestamp(item: FinishedUser?) {
    item?.let {
        text = item.timestamp
    }
}

@BindingAdapter("userFinishedRadio")
fun CheckBox.setUserFinishedRadio(item: FinishedUser?) {
    item?.let {
        if (it.timestamp != "N/A") {
            this.isChecked = true
        }
    }
}

@BindingAdapter(
    value = ["android:setRadioExercise", "android:setRadioUserEmail"],
    requireAll = true
)
fun CheckBox.setRadio(item: Exercise?, userEmail: String?) {
    item?.let { exercise ->
        userEmail?.let {
            Timber.wtf("exercise setRadio -> ${exercise.finishedUsersList}" )
            this.isChecked = exercise.finishedUsersList[it] != "N/A"
        }
    }
}













