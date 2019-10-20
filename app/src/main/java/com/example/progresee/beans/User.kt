package com.example.progresee.beans

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class User(
    @PrimaryKey
    val uid: String,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String,
    @SerializedName("dateCreated") val dateCreated: DateCreated,
    @SerializedName("signedIn") val signedIn: DateCreated,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String
)
