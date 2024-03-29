package com.swef.cookcode.data

data class UserData(
    val userId: Int,
    val nickname: String,
    val profileImage: String?,
    var subscribed: Boolean,
    var subscriberCount: Int
)
