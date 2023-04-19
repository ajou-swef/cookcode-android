package com.swef.cookcode.data.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("status")val status: Int,
    @SerializedName("data")val tokenData: TokenData
)

data class TokenData(
    @SerializedName("userId")val userId: Int,
    @SerializedName("accessToken")val accessToken: String,
    //refreshToken은 로그인할 때 주어지고 토큰 재발행 시 사용되므로
    //response를 재사용하기 위해 nullable 표현 적용
    @SerializedName("refreshToken")val refreshToken: String?
)
