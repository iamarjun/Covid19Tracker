package com.arjun.covid19tracker.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Covid(
    @Json(name = "Countries")
    val countries: List<Country> = listOf(),
    @Json(name = "Date")
    val date: String = "",
    @Json(name = "Global")
    val global: Global = Global()
)