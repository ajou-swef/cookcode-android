package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val commentData: List<Comment>
)

data class Comment(
    @SerializedName("id") val commentId: Int,
    @SerializedName("user") val madeUser: MadeUser,
    @SerializedName("comment") val comment: String
)