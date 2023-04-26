package com.swef.cookcode.data

import android.net.Uri
import com.swef.cookcode.R

data class StepImageData(
    // 갤러리에서 불러올 이미지
    val imageUri: Uri? = null,
    // 기본 이미지
    val basicImage: Int = R.drawable.upload_image
)
