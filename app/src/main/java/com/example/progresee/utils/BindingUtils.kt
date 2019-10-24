package com.example.progresee.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.progresee.R
import com.example.progresee.beans.Classroom
import com.example.progresee.beans.Exercise
import com.example.progresee.beans.Task
import com.example.progresee.beans.User
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit




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

@BindingAdapter("setOwner","setClassroom")
fun TextView.setOwner(user: User?,classroom: Classroom?) {
    user?.let {
        classroom?.let {
            if (user.email==classroom.owner)
                text = "owner"
        }

    }
}


//TODO change time zones
@BindingAdapter("setLastLoggedIn")
fun TextView.setLastLoggedIn(user: User?) {
    user?.let {
        val seconds=user.signedIn.seconds+0L
        val nano=user.signedIn.nanos+3600000000000*3
        val mil=TimeUnit.MILLISECONDS.convert(nano,TimeUnit.NANOSECONDS)
        val millis = TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault())
        val netDate = Date(millis+mil)
        val date = sdf.format(netDate).toString()
        text = context.getString(R.string.last_login_date, date)
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

@BindingAdapter("taskDueDate")
fun TextView.setTaskDueDate(item: Task?) {
    item?.let {
        text = context.getString(R.string.due_by, item.endDate.toString())
    }
}

@BindingAdapter("setTaskStatus")
fun ImageView.setTaskStatus(item:Task?) {
    item?.let {
        if (it.status){
            this.setImageResource(R.drawable.ic_lock_open_24px)
        } else   this.setImageResource(R.drawable.ic_lock_24px)
    }
}

@BindingAdapter("taskDescription")
fun TextView.setTaskDescription(item: Task?) {
    item?.let {
        text = item.description
    }
}

@BindingAdapter("taskImage")
fun ImageView.setTaskImage(item: Task?) {
    item?.let {
        Glide.with(context)
            .load(item.imageUrl)
            .into(this)
    }
}

@BindingAdapter("setLinks")
fun TextView.setLinks(item:Task?) {
    item?.let {
        text=it.referenceLink
    }
}

@BindingAdapter("exerciseText")
fun TextView.setExerciseText(item: Exercise?) {
    item?.let {
        text = item.exerciseTitle
    }
}












