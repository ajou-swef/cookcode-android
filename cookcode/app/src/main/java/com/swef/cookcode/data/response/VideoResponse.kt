package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class videoUrl(
    @SerializedName("videoUrl") val videoUris: List<String>
)

data class VideoResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val videoUris: videoUrl
)
