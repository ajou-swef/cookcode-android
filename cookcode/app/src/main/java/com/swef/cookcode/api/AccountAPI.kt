package com.swef.cookcode.api

import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.TokenResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AccountAPI {

    @POST("account/signup")
    fun postUserData(
        @Body body: HashMap<String, Any>
    ): Call<StatusResponse>

    @GET("account/check")
    fun getDupNickTest(
        @Query("nickname")nickname: String
    ): Call<DuplicateResponse>

    @GET("account/check")
    fun getDupEmailTest(
        @Query("email")email: String
    ): Call<DuplicateResponse>

    @POST("account/signin")
    fun postSignin(
        @Query("email")email: String,
        @Query("password")pw: String
    ): Call<TokenResponse>

    @GET("account/token/reissue")
    fun getReissue(
        @Body body: HashMap<Any, Any>
    ): Call<TokenResponse>

    @DELETE("account")
    fun deleteAccount(): Call<Any>

    companion object {
        private const val BASE_URL = "api/v1/"

        fun create(): AccountAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AccountAPI::class.java)
        }
    }
}