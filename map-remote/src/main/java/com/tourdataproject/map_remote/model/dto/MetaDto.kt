package com.tourdataproject.map_remote.model.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MetaDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("pageable_count") val pageableCount: Int,
    @SerializedName("is_end") val isEnd: Boolean
)