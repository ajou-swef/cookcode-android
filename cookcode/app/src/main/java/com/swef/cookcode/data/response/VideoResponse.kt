package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class VideoUrl(
    @SerializedName("videoUrl") val videoUris: List<String>
)
