package com.swef.cookcode.api

import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.TokenResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @GET("account/token/reissue")
    fun reissueToken(
        @Header("accessToken") accessToken: String,
        @Query("refreshToken") refreshToken: String
    ): Call<TokenResponse>

    @POST("account/signup")
    fun postUserData(
        @Body body: HashMap<String, String>
    ): Call<StatusResponse>

    @GET("account/check")
    fun getDupNickTest(
        @Query("nickname")nickname: String
    ): Call<DuplicateResponse>

    @POST("account/signin")
    fun postSignin(
        @Body body: HashMap<String, String>
    ): Call<TokenResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): AuthService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthService::class.java)
        }
    }
}