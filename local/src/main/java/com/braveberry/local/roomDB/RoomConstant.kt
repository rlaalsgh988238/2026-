package com.braveberry.local.roomDB

internal object RoomConstant {
    const val DB_NAME = "local_database"
    const val ROOM_VERSION = 2

    object Table{
        const val TOILET = "toilet"
        const val USER = "user"
        const val COURSE = "course"
        const val REGION = "region"
        const val BACKUP = "backUp"
    }
}