package com.example.progresee.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

@Entity
data class Task(

    @PrimaryKey
    val uid: String,
    var title: String,
    var description: String,
    var imageUrl: String,
    var referenceLink: String,
    val startDate: DateCreated,
    var endDate: DateCreated,
    val classroomUid: String,
    var status: Boolean

)