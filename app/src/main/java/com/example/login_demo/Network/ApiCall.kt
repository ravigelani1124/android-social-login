package com.example.login_demo.Network

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiCall {

    @POST("access_token")
    @FormUrlEncoded
    fun getRequestToken(
        @Field("client_id") client_id: String?,
        @Field("client_secret") client_secret: String?,
        @Field("grant_type") grant_type: String?,
        @Field("redirect_uri") redirect_uri: String?,
        @Field("code") code: String?
    ): Call<JsonObject?>?

    @GET("me")
    fun getUserInfo(
        @Query("fields") fields: String?,
        @Query("access_token") access_token: String?

    ): Call<JsonObject?>?
}