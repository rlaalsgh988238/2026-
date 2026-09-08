package com.tourdataproject.tourdata_remote.impl


import com.braveberry.data_resource.DataResource
import com.tourdataproject.dataSource.TourDataSource
import com.tourdataproject.model.TourDataModel
import com.tourdataproject.tourdata_remote.api.TourApiService
import com.tourdataproject.tourdata_remote.mapper.toRemoteModel
import com.tourdataproject.tourdata_remote.model.toDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TourDataSourceImpl @Inject constructor(
    private val tourApiService: TourApiService
) : TourDataSource {

    override fun getContentId(
        lat: Double,
        lng: Double,
        radius: Int
    ): Flow<DataResource<String>> = flow {
        emit(DataResource.Loading())
        try {
            val response = tourApiService.getLocationBasedList(
                mapX = lng.toString(),
                mapY = lat.toString(),
                radius = radius.toString()
            )

            val firstItem = response.response.body?.items?.item?.firstOrNull()

            if (firstItem != null) {
                emit(DataResource.Success(firstItem.contentId))
            } else {
                emit(DataResource.Error(NoSuchElementException("해당 반경 내에 관광지 정보가 없습니다.")))
            }
        } catch (e: Exception) {
            emit(DataResource.Error(e))
        }
    }

    override fun getTourDataToiletInfo(
        contentId: String
    ): Flow<DataResource<TourDataModel>> = flow {
        emit(DataResource.Loading())
        try {
            val response = tourApiService.getDetailWithTour(contentId = contentId)

            val firstItem = response.response.body?.items?.item?.firstOrNull()

            if (firstItem != null) {
                val dataModel = firstItem.toRemoteModel().toDataModel()
                emit(DataResource.Success(dataModel))
            } else {
                emit(DataResource.Error(NoSuchElementException("해당 contentId의 무장애 정보를 찾을 수 없습니다.")))
            }
        } catch (e: Exception) {
            emit(DataResource.Error(e))
        }
    }
}
