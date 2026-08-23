package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.RoomConstant

@Dao
internal interface ToiletDataDao: BaseDao<ToiletDataLocalModel> {
    @Query("SELECT * FROM ${RoomConstant.Table.TOILET} WHERE id = :toiletId")
    suspend fun getToiletData(toiletId: String): ToiletDataLocalModel?

    @Query("SELECT * FROM ${RoomConstant.Table.TOILET}")
    suspend fun getAllToiletData(): List<ToiletDataLocalModel>

    @Query("SELECT * FROM ${RoomConstant.Table.TOILET} WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getToiletsInBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<ToiletDataLocalModel>

    @Query("""
    SELECT * FROM ${RoomConstant.Table.TOILET} 
    WHERE toiletName LIKE :name || '%' 
       OR toiletName LIKE '% ' || :name || '%'
    ORDER BY 
        CASE 
            WHEN toiletName LIKE :name || '%' THEN 1 -- 이름으로 바로 시작하는 걸 1순위 
            ELSE 2                              -- 중간 단어 시작(경기 가평 등)을 2순위
        END, 
        toiletName ASC -- 같은 순위 내에서는 가나다순
""")
    suspend fun getSimilarNameToiletData(name: String): List<ToiletDataLocalModel>
}