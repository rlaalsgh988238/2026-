package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.ToiletDataLocal
import com.braveberry.local.roomDB.RoomConstant

@Dao
internal interface ToiletDataDao: BaseDao<ToiletDataLocal> {
    @Query("SELECT * FROM ${RoomConstant.Table.TOILET} WHERE id = :toiletId")
    suspend fun getToiletData(toiletId: String): ToiletDataLocal?

    @Query("SELECT * FROM ${RoomConstant.Table.TOILET}")
    suspend fun getAllToiletData(): List<ToiletDataLocal>
}