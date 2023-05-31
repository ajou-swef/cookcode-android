package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val content: CommentContent
)

data class CommentContent(
    @SerializedName("content") val comments: List<Comment>,
    @SerializedName("numberOfElements") val numberOfElements: Int,
    @SerializedName("hasNext") val hasNext: Boolean
)

data class Comment(
    @SerializedName("commentId") val commentId: Int,
    @SerializedName("user") val madeUser: MadeUser,
    @SerializedName("comment") val comment: String
)