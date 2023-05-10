package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class videoUrl(
    @SerializedName("videoUrl") val videoUris: List<String>
)

data class VideoResponse(
    @SerializedName("data") val videoUris: videoUrl
)
