package com.arjun.covid19tracker

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.covid19tracker.model.Country
import com.arjun.covid19tracker.model.Global
import com.arjun.covid19tracker.model.Resource
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel @ViewModelInject constructor(private val restApi: RestApi) : ViewModel() {

    private val _countryList = MutableLiveData<Resource<List<Country>>>()
    private val _originalCountryList = MutableLiveData<List<Country>>()
    private val _globalData = MutableLiveData<Resource<Global>>()


    val countryList: LiveData<Resource<List<Country>>>
        get() = _countryList

    val globalData: LiveData<Resource<Global>>
        get() = _globalData

    val originalCountryList: LiveData<List<Country>>
        get() = _originalCountryList


    fun getLatestCovidUpdates() {
        viewModelScope.launch {
            try {
                _countryList.value = Resource.Loading(null)
                _globalData.value = Resource.Loading(null)

                val response = restApi.getCovidUpdate()

                _countryList.value =
                    Resource.Success(response.countries.sortedByDescending { it.totalConfirmed.toInt() })
                _originalCountryList.value = response.countries.sortedByDescending { it.totalConfirmed.toInt() }
                _globalData.value = Resource.Success(response.global)

            } catch (e: Exception) {

                Timber.d("fetch district stats failed ${e.message}")
                _countryList.value = Resource.Error(e.localizedMessage)
                _globalData.value = Resource.Error(e.localizedMessage)

            }
        }
    }

}