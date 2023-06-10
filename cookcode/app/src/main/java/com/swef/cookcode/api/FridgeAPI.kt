package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.FridgeResponse
import com.swef.cookcode.data.response.MyIngredList
import com.swef.cookcode.data.response.StatusResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FridgeAPI {
    @GET("fridge/")
    fun getFridgeData(): Call<MyIngredList>

    @POST("fridge/ingred")
    fun postIngredientData(
        @Body body: HashMap<String, Any>
    ): Call<FridgeResponse>

    @DELETE("fridge/ingred/{fridgeIngredId}")
    fun deleteIngredientData(
        @Path("fridgeIngredId") fridgeId: Int
    ): Call<StatusResponse>

    @PATCH("fridge/ingred/{fridgeIngredId}")
    fun patchIngredientData(
        @Path("fridgeIngredId") fridgeId: Int,
        @Body body: HashMap<String, Any>
    ): Call<StatusResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): FridgeAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FridgeAPI::class.java)
        }
    }
}