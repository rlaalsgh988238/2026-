package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.course.CourseLocalModel
import com.braveberry.local.roomDB.RoomConstant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CourseDao : BaseDao<CourseLocalModel> {


    @Query("SELECT * FROM ${RoomConstant.Table.COURSE}")
    fun getAllCourses(): Flow<List<CourseLocalModel>>

    @Query("SELECT * FROM ${RoomConstant.Table.COURSE} WHERE courseId = :courseId")
    fun getCourseById(courseId: String): Flow<CourseLocalModel?>

    @Query("DELETE FROM ${RoomConstant.Table.COURSE} WHERE courseId = :courseId")
    suspend fun deleteCourseById(courseId: String)
}