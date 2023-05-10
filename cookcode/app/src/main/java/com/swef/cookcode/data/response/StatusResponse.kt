package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class StatusResponse(
    @SerializedName("message")val message: String,
    @SerializedName("status")val status: Int
)
