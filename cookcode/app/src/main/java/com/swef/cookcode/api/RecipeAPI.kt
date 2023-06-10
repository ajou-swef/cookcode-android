package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.CommentResponse
import com.swef.cookcode.data.response.FileResponse
import com.swef.cookcode.data.response.RecipeContentResponse
import com.swef.cookcode.data.response.RecipeResponse
import com.swef.cookcode.data.response.RecipeStatusResponse
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

interface RecipeAPI {

    @Multipart
    @JvmSuppressWildcards
    @POST("recipe/files/recipe")
    fun postImage(
        @Part file: MultipartBody.Part
    ): Call<FileResponse>

    @POST("recipe")
    fun postRecipe(
        @Body body: HashMap<String, Any>
    ): Call<RecipeStatusResponse>

    @GET("recipe")
    fun getRecipes(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "createdAt",
        // @Query("month") month: Int,
        @Query("cookable") cookable: Int
    ): Call<RecipeResponse>

    @GET("recipe/{recipeId}")
    fun getRecipe(
        @Path("recipeId") recipeId: Int
    ): Call<RecipeContentResponse>

    @DELETE("recipe/{recipeId}")
    fun deleteRecipe(
        @Path("recipeId") recipeId: Int
    ): Call<StatusResponse>

    @PATCH("recipe/{recipeId}")
    fun patchRecipe(
        @Path("recipeId") recipeId: Int,
        @Body body: HashMap<String, Any>
    ): Call<RecipeResponse>

    @GET("recipe/{recipeId}/comments")
    fun getRecipeComments(
        @Path("recipeId") recipeId: Int
    ): Call<CommentResponse>

    @POST("recipe/{recipeId}/comments")
    fun putRecipeComment(
        @Path("recipeId") recipeId: Int,
        @Body body: Map<String, String>
    ): Call<StatusResponse>

    @DELETE("recipe/comments/{commentId}")
    fun deleteRecipeComment(
        @Path("commentId") commentId: Int,
    ): Call<StatusResponse>

    @POST("recipe/{recipeId}/likes")
    fun putLikeStatus(
        @Path("recipeId") commentId: Int,
    ): Call<StatusResponse>

    @GET("recipe/search")
    fun getSearchRecipes(
        @Query("query") keyword: String,
        @Query("cookable") cookable: Int,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Call<RecipeResponse>

    @GET("recipe/user/{targetUserId}")
    fun getUserRecipes(
        @Path("targetUserId") userId: Int,
        @Query("page") page: Int,
    ): Call<RecipeResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): RecipeAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RecipeAPI::class.java)
        }
    }
}