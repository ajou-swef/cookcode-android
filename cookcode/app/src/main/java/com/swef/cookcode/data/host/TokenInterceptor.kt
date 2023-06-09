package com.swef.cookcode.data.host

import com.swef.cookcode.api.AuthService
import com.swef.cookcode.data.GlobalVariables.refreshToken
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor() : Interceptor {
    private val authService = AuthService.create()
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalResponse = chain.proceed(originalRequest)

        if (originalResponse.code == 401) {
            val accessToken = originalRequest.header("accessToken")
            val reissueCall = authService.reissueToken(accessToken!!, refreshToken)
            val reissueResponse = reissueCall.execute()

            if (reissueResponse.isSuccessful) {
                // 토큰 재발행이 성공한 경우 새로운 액세스 토큰을 헤더에 설정합니다.
                val newAccessToken = reissueResponse.body()!!.tokenData.accessToken
                val newRequest = originalRequest.newBuilder()
                    .header("accessToken", newAccessToken)
                    .build()

                // 새로운 요청을 실행합니다.
                return chain.proceed(newRequest)
            }
        }

        return originalResponse
    }
}