package com.arjun.covid19tracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjun.covid19tracker.databinding.ActivityMainBinding
import com.arjun.covid19tracker.model.Country
import com.arjun.covid19tracker.model.Global
import com.arjun.covid19tracker.model.Resource
import com.arjun.covid19tracker.util.GpsUtils
import com.arjun.covid19tracker.util.showToast
import com.arjun.covid19tracker.util.viewbinding.viewBinding
import com.arjun.covid19tracker.util.visibility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.IOException
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    private lateinit var confirmedValue: TextView
    private lateinit var recoveredValue: TextView
    private lateinit var deceasedValue: TextView
    private lateinit var loader: ProgressBar
    private lateinit var countryList: RecyclerView

    private lateinit var viewModel: MainViewModel

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationRequest: LocationRequest by lazy { LocationRequest.create() }

    private var isContinue = false
    private var isGPS = false

    private val countryListAdapter by lazy { CountryListAdapter() }
    private val headerAdapter by lazy { HeaderAdapter() }
    private val myCountryAdapter by lazy { MyCountryAdapter() }

    private var wayLatitude = 0.0
    private var wayLongitude = 0.0

    private var countryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10 * 1000.toLong() // 10 seconds
        locationRequest.fastestInterval = 5 * 1000.toLong() // 5 seconds

        GpsUtils(this).turnGPSOn(object : GpsUtils.OnGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        confirmedValue = binding.textConfirmedValue
        recoveredValue = binding.textRecoveredValue
        deceasedValue = binding.textDeceasedValue
        loader = binding.loader
        countryList = binding.countryList

        val concatAdapter = ConcatAdapter(headerAdapter, myCountryAdapter, countryListAdapter)

        countryList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = concatAdapter
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constants.LOCATION_REQUEST
            )
        } else {
            mFusedLocationClient.lastLocation
                .addOnSuccessListener(this@MainActivity) { location ->
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        Timber.d(
                            String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                        )
                        countryName = getCountryName(wayLatitude, wayLongitude)
                        Timber.d(countryName)
                        viewModel.getLatestCovidUpdates()

                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, null)
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()

        isContinue = false
        getLocation()

        viewModel.globalData.observe(this) {
            when (it) {

                is Resource.Loading -> {
                    Timber.d(it.message)
                    loader.visibility(true)

                }
                is Resource.Success -> {
                    Timber.d(it.data.toString())
                    loader.visibility(false)
                    it.data?.let { globalData ->
                        setGlobalData(globalData)
                    }
                }
                is Resource.Error -> {
                    Timber.d(it.message)
                    loader.visibility(false)
                }
            }
        }

        viewModel.countryList.observe(this) {
            when (it) {

                is Resource.Loading -> {
                    Timber.d(it.message)
                    loader.visibility(true)

                }
                is Resource.Success -> {
                    Timber.d(it.data.toString())
                    loader.visibility(false)
                    it.data?.let { countries ->
                        headerAdapter.submitList(
                            listOf(
                                Country(
                                    country = "COUNTRY",
                                    totalConfirmed = "TOTAL CASES",
                                    totalDeaths = "RECOVERED",
                                    totalRecovered = "DEATHS"
                                )
                            )
                        )
                        val myCountry = countries.filter {
                            it.country.equals(countryName, ignoreCase = true)
                        }
                        myCountryAdapter.submitList(myCountry)
                        countryListAdapter.submitList(countries)
                    }
                }
                is Resource.Error -> {
                    Timber.d(it.message)
                    loader.visibility(false)
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.lastLocation
                        .addOnSuccessListener(this@MainActivity) { location ->
                            if (location != null) {
                                wayLatitude = location.latitude
                                wayLongitude = location.longitude
                                Timber.d(
                                    String.format(Locale.US, "%s - %s", wayLatitude, wayLongitude)
                                )

                                countryName = getCountryName(wayLatitude, wayLongitude)
                                Timber.d(countryName)
                                viewModel.getLatestCovidUpdates()

                            } else {
                                mFusedLocationClient.requestLocationUpdates(locationRequest, null)
                            }
                        }
                } else {
                    showToast("Permission denied", Toast.LENGTH_SHORT)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_REQUEST) {
                isGPS = true // flag maintain before get location
            }
        }
    }

    private fun setGlobalData(globalData: Global) {
        confirmedValue.text = globalData.totalConfirmed.toString()
        recoveredValue.text = globalData.totalRecovered.toString()
        deceasedValue.text = globalData.totalDeaths.toString()
    }

    private fun getCountryName(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            return if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].countryName
            } else null
        } catch (ignored: IOException) {
            //do something
            Timber.d(ignored)
        }

        return null
    }

    companion object {


    }


}