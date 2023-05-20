package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class FileResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val fileUrls: FileUrl
)


data class FileUrl(
    @SerializedName("urls") val listUrl: List<String>
)

