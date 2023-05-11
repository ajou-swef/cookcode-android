package com.swef.cookcode.api

import com.swef.cookcode.data.response.FridgeResponse
import com.swef.cookcode.data.response.MyIngredList
import com.swef.cookcode.data.response.StatusResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FridgeAPI {
    @GET("fridge/")
    fun getFridgeData(
        @Header("accessToken") accessToken: String
    ): Call<MyIngredList>

    @POST("fridge/ingred")
    fun postIngredientData(
        @Header("accessToken") accessToken: String,
        @Body body: HashMap<String, Any>
    ): Call<FridgeResponse>

    @DELETE("fridge/ingred/{fridgeIngredId}")
    fun deleteIngredientData(
        @Header("accessToken") accessToken: String,
        @Path("fridgeIngredId") fridgeId: Int
    ): Call<StatusResponse>

    @PATCH("fridge/ingred/{fridgeIngredId}")
    fun patchIngredientData(
        @Header("accessToken") accessToken: String,
        @Path("fridgeIngredId") fridgeId: Int,
        @Body body: HashMap<String, Any>
    ): Call<StatusResponse>

    companion object {
        private const val BASE_URL = "http://54.180.117.179:8080/api/v1/"

        fun create(): FridgeAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FridgeAPI::class.java)
        }
    }
}