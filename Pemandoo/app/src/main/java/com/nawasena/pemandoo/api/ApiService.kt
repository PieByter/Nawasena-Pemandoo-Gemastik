package com.nawasena.pemandoo.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api")
    fun getAllLandmarks(): Call<List<LandmarkResponseItem>>

    @POST("api")
    fun createLandmark(@Body landmark: LandmarkResponseItem): Call<LandmarkResponseItem>

    @GET("api/{id}")
    fun getLandmarkById(@Path("id") id: String): Call<LandmarkResponseItem>
}
