package com.swef.cookcode.api

import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.TokenResponse
import com.swef.cookcode.data.response.UserResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountAPI {

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

    @GET("account/token/reissue")
    fun getReissue(
        @Body body: HashMap<Any, Any>
    ): Call<TokenResponse>

    @PATCH("account")
    fun patchAccount(
        @Header("accessToken") accessToken: String
    ): Call<StatusResponse>

    @GET("account/{userId}")
    fun getUserInfo(
        @Header("accessToken") accessToken: String,
        @Path("userId") userId: Int
    ):Call<UserResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): AccountAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AccountAPI::class.java)
        }
    }
}