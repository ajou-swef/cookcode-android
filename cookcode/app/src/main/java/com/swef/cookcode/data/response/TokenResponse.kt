package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("status")val status: Int,
    @SerializedName("data")val tokenData: TokenData
)

data class TokenData(
    @SerializedName("userId")val userId: Int,
    @SerializedName("accessToken")val accessToken: String,
    @SerializedName("refreshToken")val refreshToken: String
)
