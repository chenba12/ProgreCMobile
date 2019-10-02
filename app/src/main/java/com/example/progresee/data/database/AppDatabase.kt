package com.example.progresee.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.progresee.beans.*
import com.example.progresee.data.database.Dao.*
import com.example.progresee.utils.ConverterUtils

@TypeConverters(ConverterUtils::class)
@Database(entities = [Classroom::class,User::class,FinishedUsers::class,Task::class,Exercise::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classroomDao(): ClassroomDao

    abstract  fun exerciseDao():ExerciseDao

    abstract fun taskDao():TaskDao

    abstract fun userDao():UserDao

    abstract  fun finishedUsersDao():FinishedUsersDao


}