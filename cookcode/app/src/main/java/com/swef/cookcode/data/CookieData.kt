package com.swef.cookcode.data

import com.swef.cookcode.data.response.MadeUser

data class CookieData(
    val cookieId: Int,
    val videoUrl: String,
    val madeUser: MadeUser,
    val createdAt: String,
    var isLiked: Boolean
)
