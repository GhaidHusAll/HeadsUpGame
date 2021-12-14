package com.example.headsup_ghh

import retrofit2.Call
import retrofit2.http.GET

interface GetAPI {

    @GET("/celebrities/")
    fun GetData(): Call<Celebrities>
}