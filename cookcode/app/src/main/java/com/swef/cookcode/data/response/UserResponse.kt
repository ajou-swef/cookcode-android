package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val userData: UserData
)

data class UserData(
    @SerializedName("userId") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("authority") val authority: String,
    @SerializedName("status") val accountStatus: String
)