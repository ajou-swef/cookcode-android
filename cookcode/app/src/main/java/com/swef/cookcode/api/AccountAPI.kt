package com.swef.cookcode.api

import com.swef.cookcode.data.response.DuplicateResponse
import com.swef.cookcode.data.response.ProfileImageResponse
import com.swef.cookcode.data.response.SearchUserResponse
import com.swef.cookcode.data.response.StatusResponse
import com.swef.cookcode.data.response.TokenResponse
import com.swef.cookcode.data.response.UserResponse
import com.swef.cookcode.data.response.UsersResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
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
    ): Call<UserResponse>

    @POST("account/subscribe/{createrId}")
    fun postUserSubscribe(
        @Header("accessToken") accessToken: String,
        @Path("createrId") userId: Int
    ): Call<StatusResponse>

    @DELETE("account/subscribe/{createrId}")
    fun deleteUserSubscribe(
        @Header("accessToken") accessToken: String,
        @Path("createrId") userId: Int
    ): Call<StatusResponse>

    @GET("account/subscribe/subscribers")
    fun getMySubscribers(
        @Header("accessToken") accessToken: String,
    ): Call<UsersResponse>

    @GET("account/subscribe/publishers")
    fun getMyPublishers(
        @Header("accessToken") accessToken: String,
    ): Call<UsersResponse>

    @GET("account/search")
    fun getSearchUsers(
        @Header("accessToken") accessToken: String,
        @Query("nickname") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Call<SearchUserResponse>

    @Multipart
    @JvmSuppressWildcards
    @PATCH("account/profileImage")
    fun patchProfileImage(
        @Header("accessToken") accessToken: String,
        @Part partList: List<MultipartBody.Part>?
    ): Call<ProfileImageResponse>

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