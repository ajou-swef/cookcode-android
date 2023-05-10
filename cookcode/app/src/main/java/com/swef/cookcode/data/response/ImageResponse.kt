package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class photoUrl(
    @SerializedName("photoUrl") val imageUris: List<String>
)

data class ImageResponse(
    @SerializedName("data") val imageUris: photoUrl
)
