package com.braveberry.local.model.system

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braveberry.local.roomDB.RoomConstant

@Entity(tableName = RoomConstant.Table.BACKUP)
internal data class BackUpLocalModel(
    @PrimaryKey
    val id: String = "PLAN_BACKUP_KEY", // 단일 백업이므로 키를 고정
    val jsonContent: String
)
