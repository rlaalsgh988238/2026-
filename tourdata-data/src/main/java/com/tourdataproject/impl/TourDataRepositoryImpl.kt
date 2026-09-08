package com.tourdataproject.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapDataResource
import com.tourdataproject.dataSource.TourDataSource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.ScheduleItem
import com.tourdataproject.domain.repository.TourRepository
import com.tourdataproject.mapper.toDomainModel
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import sun.jvm.hotspot.HelloWorld.e

class TourDataRepositoryImpl(
    private val tourDataSource: TourDataSource
) : TourRepository {
    override fun getContentId(
        lat: Double,
        lng: Double,
        radius: Int
    ): Flow<DataResource<String>> {
        return tourDataSource.getContentId(lat, lng, radius)
    }


    override fun getTourDataToiletInfo(contentId: String): Flow<DataResource<AccessibilityInfo>> =
        flow {
            emit(DataResource.Loading())

            emitAll(
                tourDataSource.getTourDataToiletInfo(contentId)
                    .mapDataResource { dataModel ->
                        dataModel.toDomainModel()
                    }
            )

        }

}
