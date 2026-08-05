package com.tourdataproject.map_remote.response

import com.google.gson.annotations.SerializedName

data class DocumentDto(
    @SerializedName("address_name")
    val addressName: String,

    //필요 정보 일단 x,y 값만 있으면 될 듯 하다
    @SerializedName("x")
    val longitude: String,

    @SerializedName("y")
    val latitude: String,

)