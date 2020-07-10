package com.arjun.covid19tracker

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.covid19tracker.model.Country
import com.arjun.covid19tracker.model.Global
import com.arjun.covid19tracker.model.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel @ViewModelInject constructor(private val restApi: RestApi) : ViewModel() {

    private val _countryList by lazy { MutableLiveData<Resource<List<Country>>>() }
    private val _originalCountryList by lazy { MutableLiveData<List<Country>>() }
    private val _globalData by lazy { MutableLiveData<Resource<Global>>() }
    private val _lastUpdatedTime by lazy { MutableLiveData<String>() }


    val countryList: LiveData<Resource<List<Country>>>
        get() = _countryList

    val globalData: LiveData<Resource<Global>>
        get() = _globalData

    val originalCountryList: LiveData<List<Country>>
        get() = _originalCountryList

    val lastUpdatedTime: LiveData<String>
        get() = _lastUpdatedTime


    fun getLatestCovidUpdates() {
        viewModelScope.launch {
            while (true) {
                fetchUpdates()
                delay(UPDATE_INTERVAL)
            }
        }
    }

    private suspend fun fetchUpdates() {
        try {
            _countryList.value = Resource.Loading(null)
            _globalData.value = Resource.Loading(null)

            val response = restApi.getCovidUpdate()

            _countryList.value =
                Resource.Success(response.countries.sortedByDescending { it.totalConfirmed.toInt() })
            _originalCountryList.value =
                response.countries.sortedByDescending { it.totalConfirmed.toInt() }
            _globalData.value = Resource.Success(response.global)

            _lastUpdatedTime.value = getLastUpdatedDate(System.currentTimeMillis())

        } catch (e: Exception) {

            Timber.d("fetch district stats failed ${e.message}")
            _countryList.value = Resource.Error(e.localizedMessage)
            _globalData.value = Resource.Error(e.localizedMessage)
        }
    }

    private fun getLastUpdatedDate(currentTime: Long ): String {
        val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.getDefault())
        val lastUpdatedDate = Date(currentTime)
        return sdf.format(lastUpdatedDate)
    }

    companion object {
        const val UPDATE_INTERVAL = 2 * 60 * 1000L
    }

}