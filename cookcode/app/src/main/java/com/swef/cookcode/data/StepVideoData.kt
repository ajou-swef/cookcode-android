package com.swef.cookcode.data

import android.graphics.Bitmap
import android.net.Uri

data class StepVideoData(
    var thumbnail: Bitmap?,
    var title: String?,
    var uri: Uri?,
    var duration: Long?,
    var width: Int?,
    var height: Int?
)
