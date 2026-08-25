package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.course.CourseLocalModel
import com.braveberry.local.roomDB.RoomConstant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CourseDao : BaseDao<CourseLocalModel> {


    @Query("SELECT * FROM ${RoomConstant.Table.COURSE}")
    suspend fun getAllCourses(): List<CourseLocalModel>

    // 🌟 2. Flow 지우고 suspend 장착!
    @Query("SELECT * FROM ${RoomConstant.Table.COURSE} WHERE courseId = :courseId")
    suspend fun getCourseById(courseId: String): CourseLocalModel?

    @Query("DELETE FROM ${RoomConstant.Table.COURSE} WHERE courseId = :courseId")
    suspend fun deleteCourseById(courseId: String)
}