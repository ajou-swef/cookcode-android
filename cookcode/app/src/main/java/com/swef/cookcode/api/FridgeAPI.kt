package com.swef.cookcode.api

import com.swef.cookcode.data.response.IngredientResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface FridgeAPI {
    @GET()
    fun getFridgeData(
        @Header("accessToken") accessToken: String
    ): Call<List<IngredientResponse>>


    companion object {
        private const val BASE_URL = "http://54.180.117.179:8080/api/v1/fridge"

        fun create(): AccountAPI {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AccountAPI::class.java)
        }
    }
}