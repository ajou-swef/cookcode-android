package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.CommentResponse
import com.swef.cookcode.data.response.CookieContentResponse
import com.swef.cookcode.data.response.CookieResponse
import com.swef.cookcode.data.response.OneCookieResponse
import com.swef.cookcode.data.response.StatusResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CookieAPI {

    @Multipart
    @JvmSuppressWildcards
    @POST("cookie")
    fun postCookie(
        @Part partList: List<MultipartBody.Part>
    ): Call<StatusResponse>

    @GET("cookie")
    fun getCookies(
        @Query("page") page: Int
    ): Call<CookieResponse>

    @GET("cookie/{cookieId}")
    fun getCookie(
        @Path("cookieId") cookieId: Int
    ): Call<OneCookieResponse>

    @PATCH("cookie/{cookieId}")
    fun patchCookie(
        @Path("cookieId") cookieId: Int,
        @Body body: Map<String, String>
    ): Call<StatusResponse>

    @DELETE("cookie/{cookieId}")
    fun deleteCookie(
        @Path("cookieId") cookieId: Int,
    ): Call<StatusResponse>

    @POST("cookie/{cookieId}/likes")
    fun putLikeCookie(
        @Path("cookieId") cookieId: Int,
    ): Call<StatusResponse>

    @GET("cookie/{cookieId}/comments")
    fun getCookieComments(
        @Path("cookieId") cookieId: Int
    ): Call<CommentResponse>

    @POST("cookie/{cookieId}/comments")
    fun putCookieComment(
        @Path("cookieId") cookieId: Int,
        @Body body: Map<String, String>
    ): Call<StatusResponse>

    @DELETE("cookie/comments/{commentId}")
    fun deleteCookieComment(
        @Path("commentId") commentId: Int,
    ): Call<StatusResponse>

    @GET("cookie/user/{userId}")
    fun getUserCookies(
        @Path("userId") userId: Int,
        @Query("page") page: Int,
    ): Call<CookieContentResponse>

    @GET("cookie/search")
    fun getSearchCookies(
        @Query("query") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int?
    ): Call<CookieContentResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): CookieAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CookieAPI::class.java)
        }
    }
}