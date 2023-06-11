package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.AuthorizationResponse
import com.swef.cookcode.data.response.StatusResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AdminAPI {

    @PATCH("admin/authorization/{userId}/{isAccept}")
    fun patchAuthorization(
        @Path("userId") userId: Int,
        @Path("isAccept") isAccept: Int,
    ): Call<StatusResponse>

    @GET("admin/authorization")
    fun getAuthorization(): Call<AuthorizationResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): AdminAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AdminAPI::class.java)
        }
    }
}