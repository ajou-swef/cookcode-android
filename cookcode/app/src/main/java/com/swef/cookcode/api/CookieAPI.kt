package com.swef.cookcode.api

import com.swef.cookcode.data.response.CommentResponse
import com.swef.cookcode.data.response.CookieResponse
import com.swef.cookcode.data.response.OneCookieResponse
import com.swef.cookcode.data.response.StatusResponse
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

interface CookieAPI {

    @Multipart
    @JvmSuppressWildcards
    @POST("cookie")
    fun postCookie(
        @Header("accessToken") accessToken: String,
        @Part partList: List<MultipartBody.Part>
    ): Call<StatusResponse>

    @GET("cookie")
    fun getCookies(
        @Header("accessToken") accessToken: String,
        @Query("page") page: Int
    ): Call<CookieResponse>

    @GET("cookie/{cookieId}")
    fun getCookie(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int
    ): Call<OneCookieResponse>

    @PATCH("cookie/{cookieId}")
    fun patchCookie(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int,
        @Body body: Map<String, String>
    ): Call<StatusResponse>

    @DELETE("cookie/{cookieId}")
    fun deleteCookie(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int,
    ): Call<StatusResponse>

    @POST("cookie/{cookieId}/likes")
    fun putLikeCookie(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int,
    ): Call<StatusResponse>

    @GET("cookie/{cookieId}/comments")
    fun getCookieComments(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int
    ): Call<CommentResponse>

    @POST("cookie/{cookieId}/comments")
    fun putCookieComment(
        @Header("accessToken") accessToken: String,
        @Path("cookieId") cookieId: Int,
        @Body body: Map<String, String>
    ): Call<StatusResponse>

    @DELETE("cookie/comments/{commentId}")
    fun deleteCookieComment(
        @Header("accessToken") accessToken: String,
        @Path("commentId") commentId: Int,
    ): Call<StatusResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): CookieAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CookieAPI::class.java)
        }
    }
}