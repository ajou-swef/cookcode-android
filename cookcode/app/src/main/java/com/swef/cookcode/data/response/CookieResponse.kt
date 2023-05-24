package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class CookieResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: CookieResponseData
)

data class CookieResponseData(
    @SerializedName("content") val cookies: List<CookieContent>,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class CookieContent(
    @SerializedName("videoUrl") val videoUrl: String,
    @SerializedName("desc") val description: String,
    @SerializedName("title") val title: String,
    @SerializedName("id") val cookieId: Int
)
