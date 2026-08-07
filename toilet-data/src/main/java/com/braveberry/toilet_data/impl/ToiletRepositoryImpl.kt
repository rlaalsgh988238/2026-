package com.braveberry.toilet_data.impl

import com.braveberry.toilet_data.localDB.ToiletDataSource
import javax.inject.Inject

internal class ToiletRepositoryImpl @Inject constructor(
    private val toiletDataSource: ToiletDataSource
){

}