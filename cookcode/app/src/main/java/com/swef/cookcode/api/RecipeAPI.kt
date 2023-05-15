package com.swef.cookcode.api

import com.swef.cookcode.data.response.ImageResponse
import com.swef.cookcode.data.response.RecipeContentResponse
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
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeAPI {

    @Multipart
    @Headers("Content-Type: multipart/form-data")
    @POST("recipe/photos")
    fun postImage(
        @Header("accessToken") accessToken: String,
        @Part("stepImages") file: MultipartBody.Part
    ): Call<ImageResponse>

    @Multipart
    @Headers("Content-Type: multipart/form-data")
    @POST("recipe/videos")
    fun postVideo(
        @Header("accessToken") accessToken: String,
        @Part("stepVideos") file: MultipartBody.Part
    ): Call<VideoResponse>

    @POST("recipe")
    fun postRecipe(
        @Header("accessToken") accessToken: String,
        @Body body: HashMap<String, Any>
    ): Call<RecipeStatusResponse>

    @GET("recipe")
    fun getRecipes(
        @Header("accessToken") accessToken: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        // @Query("sort") sort: String = "createdAt",
        // @Query("month") month: Int,
        // @Query("cookable") cookable: Int
    ): Call<RecipeResponse>

    @GET("recipe/{recipeId}")
    fun getRecipe(
        @Header("accessToken") accessToken: String,
        @Path("recipeId") recipeId: Int
    ): Call<RecipeContentResponse>

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