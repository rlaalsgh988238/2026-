package com.braveberry.local.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.braveberry.data_resource.DataResource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**추후 필요시 카메라, 갤러리, 알림 등 권한 설정 가능
 * */
internal class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}