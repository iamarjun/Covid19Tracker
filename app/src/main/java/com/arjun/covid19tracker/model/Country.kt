package com.arjun.covid19tracker.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "Country")
    val country: String = "",
    @Json(name = "CountryCode")
    val countryCode: String = "",
    @Json(name = "Date")
    val date: String = "",
    @Json(name = "NewConfirmed")
    val newConfirmed: Int = 0,
    @Json(name = "NewDeaths")
    val newDeaths: Int = 0,
    @Json(name = "NewRecovered")
    val newRecovered: Int = 0,
    @Json(name = "Premium")
    val premium: Premium = Premium(),
    @Json(name = "Slug")
    val slug: String = "",
    @Json(name = "TotalConfirmed")
    val totalConfirmed: Int = 0,
    @Json(name = "TotalDeaths")
    val totalDeaths: Int = 0,
    @Json(name = "TotalRecovered")
    val totalRecovered: Int = 0
)