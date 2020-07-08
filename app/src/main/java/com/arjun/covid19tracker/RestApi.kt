package com.arjun.covid19tracker

import com.arjun.covid19tracker.model.Covid
import retrofit2.http.GET

interface RestApi {

    @GET("summary")
    suspend fun getCovidUpdate(): Covid
}