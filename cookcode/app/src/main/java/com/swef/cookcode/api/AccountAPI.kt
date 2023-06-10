package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.ProfileImageResponse
import com.swef.cookcode.data.response.SearchUserResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountAPI {

    @PATCH("account")
    fun patchAccount(): Call<StatusResponse>

    @GET("account/{userId}")
    fun getUserInfo(
        @Path("userId") userId: Int
    ): Call<UserResponse>

    @POST("account/subscribe/{createrId}")
    fun postUserSubscribe(
        @Path("createrId") userId: Int
    ): Call<StatusResponse>

    @GET("account/subscribe/subscribers")
    fun getMySubscribers(): Call<SearchUserResponse>

    @GET("account/subscribe/publishers")
    fun getMyPublishers(): Call<SearchUserResponse>

    @GET("account/search")
    fun getSearchUsers(
        @Query("nickname") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Call<SearchUserResponse>

    @Multipart
    @JvmSuppressWildcards
    @PATCH("account/profileImage")
    fun patchProfileImage(
        @Part partList: List<MultipartBody.Part>?
    ): Call<ProfileImageResponse>

    @PATCH("account/password")
    fun patchPassword(
        @Body body: HashMap<String, String>
    ): Call<StatusResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): AccountAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AccountAPI::class.java)
        }
    }
}