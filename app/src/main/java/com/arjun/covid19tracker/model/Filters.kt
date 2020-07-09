package com.arjun.covid19tracker.model

sealed class Filters {
    class Total(var value: String = "", var condition: Int = 0)
    class Recovered(var value: String = "", var condition: Int = 0)
    class Deaths(var value: String = "", var condition: Int = 0)
}