package com.swef.cookcode.data

import com.swef.cookcode.data.response.MadeUser

data class CommentData(
    val madeUser: MadeUser,
    val createdAt: String,
    val comment: String,
    val commentId: Int? // 있을지 없을지 모르는 필드
)
