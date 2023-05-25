package com.swef.cookcode.api

import com.swef.cookcode.data.response.CookieResponse
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
    @POST("recipe/files/recipe")
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
        @Header("accessToken") accessToken: String
    ): Call<CookieResponse>

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

    companion object {
        private const val BASE_URL = "http://43.201.10.7:8080/api/v1/"

        fun create(): CookieAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CookieAPI::class.java)
        }
    }
}