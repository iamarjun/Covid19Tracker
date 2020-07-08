package com.arjun.covid19tracker

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjun.covid19tracker.model.Covid
import com.arjun.covid19tracker.model.Resource
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(private val restApi: RestApi) : ViewModel() {

    private val _covidData = MutableLiveData<Resource<Covid>>()
    val covidData: LiveData<Resource<Covid>>
        get() = _covidData


    fun getLatestCovidUpdates() {
        viewModelScope.launch {
            try {

                _covidData.value = Resource.Loading(null)
                val response = restApi.getCovidUpdate()
                _covidData.value = Resource.Success(response)

            } catch (e: Exception) {

                println("fetch district stats failed ${e.message}")
                _covidData.value = Resource.Error(e.localizedMessage)

            }
        }
    }

}