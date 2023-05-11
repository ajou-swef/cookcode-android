package com.swef.cookcode.api

import com.swef.cookcode.data.response.ImageResponse
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.data.response.RecipeStatusResponse
import com.swef.cookcode.data.response.VideoResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface RecipeAPI {

    @Multipart
    @POST("/recipe/photos")
    fun postImage(
        @Header("accessToken") accessToken: String,
        @Header("Content-Type") value: String = "multipart/form-data",
        @Part("stepImages") file: MultipartBody.Part
    ): Call<ImageResponse>

    @Multipart
    @POST("/recipe/videos")
    fun postVideo(
        @Header("accessToken") accessToken: String,
        @Header("Content-Type") value: String = "multipart/form-data",
        @Part("stepVideos") file: MultipartBody.Part
    ): Call<VideoResponse>

    @POST("/recipe")
    fun postRecipe(
        @Header("accessToken") accessToken: String,
        @Body body: HashMap<String, Any>
    ): Call<RecipeStatusResponse>

    @GET("/recipe")
    fun getRecipes(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "createdAt", // 현재 최신순만 설정되어있음 수정 필요
        @Query("month") month: Int,
        @Query("cookable") cookable: Boolean
    ): Call<RecipeResponse>

    companion object {
        private const val BASE_URL = "http://54.180.117.179:8080/api/v1/"

        fun create(): RecipeAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeAPI::class.java)
        }
    }
}