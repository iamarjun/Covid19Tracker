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
    val newConfirmed: String = "",
    @Json(name = "NewDeaths")
    val newDeaths: String = "",
    @Json(name = "NewRecovered")
    val newRecovered: String = "",
    @Json(name = "Premium")
    val premium: Premium = Premium(),
    @Json(name = "Slug")
    val slug: String = "",
    @Json(name = "TotalConfirmed")
    val totalConfirmed: String = "",
    @Json(name = "TotalDeaths")
    val totalDeaths: String = "",
    @Json(name = "TotalRecovered")
    val totalRecovered: String = ""
)