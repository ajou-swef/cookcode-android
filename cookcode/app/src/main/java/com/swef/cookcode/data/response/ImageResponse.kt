package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class photoUrl(
    @SerializedName("photoUrl") val listImageUri: List<String>
)

data class ImageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val imageUris: photoUrl
)
