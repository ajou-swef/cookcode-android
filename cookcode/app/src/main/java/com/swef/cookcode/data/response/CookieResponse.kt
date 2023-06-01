package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class CookieResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<CookieContent>
)

data class OneCookieResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: CookieContent
)

data class CookieContent(
    @SerializedName("videoUrl") val videoUrl: String,
    @SerializedName("desc") val description: String,
    @SerializedName("title") val title: String,
    @SerializedName("cookieId") val cookieId: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("user") val madeUser: MadeUser,
    @SerializedName("isLiked") val isLiked: Int,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("commentCount") val commentCount: Int
)
