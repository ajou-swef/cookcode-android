package com.swef.cookcode.api

import com.swef.cookcode.data.host.TokenInterceptor
import com.swef.cookcode.data.response.MembershipResponse
import com.swef.cookcode.data.response.SignedMembershipResponse
import com.swef.cookcode.data.response.StatusResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MembershipAPI {

    @GET("membership")
    fun getSignedMemberships(): Call<SignedMembershipResponse>

    @GET("membership/{createrId}")
    fun getCreatersMemberships(
        @Path("createrId") createrId: Int
    ): Call<MembershipResponse>

    @POST("membership/{membershipId}")
    fun postSigninMembership(
        @Path("membershipId") membershipId: Int
    ): Call<StatusResponse>

    @POST("membership")
    fun postCreaterMembership(
        @Body body: HashMap<String, String>
    ): Call<StatusResponse>

    @DELETE("membership/{membershipId}")
    fun deleteMembership(
        @Path("membershipId") membershipId: Int
    ): Call<StatusResponse>

    companion object {
        private const val BASE_URL = "https://cookcode.link/api/v1/"

        fun create(): MembershipAPI {
            val client = OkHttpClient.Builder()
                .addInterceptor(TokenInterceptor()) // TokenInterceptor 추가
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MembershipAPI::class.java)
        }
    }
}