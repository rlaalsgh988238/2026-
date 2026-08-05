package com.braveberry.localDB.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.localDB.model.ToiletDataLocal
import com.braveberry.localDB.roomDB.ToiletRoomConstant

@Dao
internal interface ToiletDataDao: BaseDao<ToiletDataLocal> {
    @Query("SELECT * FROM ${ToiletRoomConstant.Table.TOILET} WHERE id = :toiletId")
    suspend fun getToiletData(toiletId: String): ToiletDataLocal?

    @Query("SELECT * FROM ${ToiletRoomConstant.Table.TOILET}")
    suspend fun getAllToiletData(): List<ToiletDataLocal>
}