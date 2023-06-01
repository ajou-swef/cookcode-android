package com.swef.cookcode.data

import com.swef.cookcode.data.response.MadeUser

data class CommentData(
    val madeUser: MadeUser,
    val comment: String,
    val commentId: Int
)
