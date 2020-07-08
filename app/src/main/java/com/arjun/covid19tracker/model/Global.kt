package com.arjun.covid19tracker.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Global(
    @Json(name = "NewConfirmed")
    val newConfirmed: Int = 0,
    @Json(name = "NewDeaths")
    val newDeaths: Int = 0,
    @Json(name = "NewRecovered")
    val newRecovered: Int = 0,
    @Json(name = "TotalConfirmed")
    val totalConfirmed: Int = 0,
    @Json(name = "TotalDeaths")
    val totalDeaths: Int = 0,
    @Json(name = "TotalRecovered")
    val totalRecovered: Int = 0
)