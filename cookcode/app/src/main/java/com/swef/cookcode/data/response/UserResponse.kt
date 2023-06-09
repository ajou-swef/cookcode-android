package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val user: User
)

data class SearchUserResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val content: UserContent,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class UserContent(
    @SerializedName("content") val users: List<User>
)

data class User(
    @SerializedName("userId") val userId: Int,
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("authority") val authority: String,
    @SerializedName("status") val accountStatus: String,
    @SerializedName("profileImage") val profileImage: String?,
    @SerializedName("isSubscribed") val isSubscribed: Boolean,
    @SerializedName("subscriberCount") val subscriberCount: Int
)