package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class ProfileImageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: ProfileUrl
)

data class ProfileUrl(
    @SerializedName("urls") val url: List<String?>
)
