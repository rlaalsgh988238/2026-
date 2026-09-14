package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.system.BackUpLocalModel
import com.braveberry.local.roomDB.RoomConstant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BackUpDao : BaseDao<BackUpLocalModel> {

    // 🌟 최신 백업 상태 한 개를 가져옴 (Flow로 감싸서 실시간 감시 가능하게 설정)
    @Query("SELECT jsonContent FROM ${RoomConstant.Table.BACKUP} WHERE id = :id")
    suspend fun getBackUpById(id: String = "PLAN_BACKUP_KEY"): String?

    // 🌟 백업 전체 삭제 (초기화 시 사용)
    @Query("DELETE FROM ${RoomConstant.Table.BACKUP}")
    suspend fun clearAllBackUps()
}
