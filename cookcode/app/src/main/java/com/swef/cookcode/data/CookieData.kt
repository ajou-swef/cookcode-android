package com.swef.cookcode.data

import com.swef.cookcode.data.response.MadeUser

data class CookieData(
    val cookieId: Int,
    val videoUrl: String,
    val title: String,
    val description: String,
    val madeUser: MadeUser,
    val createdAt: String,
    var isLiked: Boolean,
    var likeNumber: Int,
    var commentCount: Int
)

data class SearchCookieData(
    val cookieId: Int,
    val thumbnail: String,
    val likeNumber: Int,
    val madeUserId: Int
)