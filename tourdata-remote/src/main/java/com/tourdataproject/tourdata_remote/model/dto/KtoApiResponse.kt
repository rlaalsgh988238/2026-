package com.tourdataproject.tourdata_remote.model.dto
import com.google.gson.annotations.SerializedName

data class KtoApiResponse<T>(
    @SerializedName("response") val response: KtoApiCore<T>
)

data class KtoApiCore<T>(
    @SerializedName("header") val header: KtoApiHeader,
    @SerializedName("body") val body: KtoApiBody<T>?
)

data class KtoApiHeader(
    @SerializedName("resultCode") val resultCode: String,
    @SerializedName("resultMsg") val resultMsg: String
)

//페이지 정보와 items를 포함
data class KtoApiBody<T>(
    @SerializedName("items") val items: KtoApiItems<T>?,
    @SerializedName("numOfRows") val numOfRows: Int,
    @SerializedName("pageNo") val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int
)


data class KtoApiItems<T>(
    @SerializedName("item") val item: List<T> // 진짜 우리가 원하는 알맹이 데이터 리스트!
)