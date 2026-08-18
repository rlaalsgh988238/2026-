package com.tourdataproject.map_remote.model.dto

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.tourdataproject.map_remote.mapper.RemoteMapper
import com.tourdataproject.map_remote.mapper.toDataModel
import com.tourdataproject.map_remote.model.MapItemRemoteModel

data class SearchResponseDto(
    @SerializedName("meta") val meta: MetaDto,
    @SerializedName("documents") val documents: List<DocumentDto>
) : RemoteMapper<ByteArray> {

    override fun toData(): ByteArray {
        // 1. 내부 documents DTO 리스트를 순회하며 깔끔한 앱 내부 모델 리스트로 변환
        val domainModels: List<MapItemRemoteModel> = this.documents.map { it.toDataModel() }

        // 2. 이제 카카오 API 스펙(DTO)이 완전히 제거된, 앱 내부 데이터만 담긴
        // 깔끔한 domainModels 리스트를 최종ByteArray로 직렬화하여 반환
        val cleanJsonString = Gson().toJson(domainModels)
        return cleanJsonString.toByteArray(Charsets.UTF_8)
    }
}