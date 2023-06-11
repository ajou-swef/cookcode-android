package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class AuthorizationResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val authorityRequest: List<AuthorityRequestUser>
)

data class AuthorityRequestUser(
    @SerializedName("userId") val userId: Int,
    @SerializedName("authority") val authority: String,
    @SerializedName("createdAt") val createdAt: String
)
