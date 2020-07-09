package com.arjun.covid19tracker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.ModalDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.arjun.covid19tracker.databinding.ActivityMainBinding
import com.arjun.covid19tracker.model.Filters
import com.arjun.covid19tracker.model.Global
import com.arjun.covid19tracker.model.Resource
import com.arjun.covid19tracker.util.GpsUtils
import com.arjun.covid19tracker.util.drawableEnd
import com.arjun.covid19tracker.util.showToast
import com.arjun.covid19tracker.util.viewbinding.viewBinding
import com.arjun.covid19tracker.util.visibility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.filter_view.view.*
import timber.log.Timber
import java.io.IOException
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    private lateinit var confirmedValue: TextView
    private lateinit var recoveredValue: TextView
    private lateinit var deceasedValue: TextView
    private lateinit var total: TextView
    private lateinit var deaths: TextView
    private lateinit var recovered: TextView
    private lateinit var appliedFilters: TextView
    private lateinit var loader: ProgressBar
    private lateinit var countryList: RecyclerView

    private lateinit var viewModel: MainViewModel

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val locationRequest: LocationRequest by lazy { LocationRequest.create() }

    private var isContinue = false
    private var isGPS = false

    private val countryListAdapter by lazy { CountryListAdapter() }
    private val myCountryAdapter by lazy { MyCountryAdapter() }

    private var wayLatitude = 0.0
    private var wayLongitude = 0.0

    private var countryName: String? = null

    private var totalDesc = true
    private var recoveredDesc = false
    private var deathsDesc = false

    private val filterText: StringBuilder by lazy { StringBuilder() }

    private val totalFilters: Filters.Total by lazy { Filters.Total() }
    private val recoveredFilters: Filters.Recovered by lazy { Filters.Recovered() }
    private val deathFilters: Filters.Deaths by lazy { Filters.Deaths() }

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
        total = binding.total
        deaths = binding.deaths
        recovered = binding.recovered
        loader = binding.loader
        countryList = binding.countryList
        appliedFilters = binding.appliedFilters

        val concatAdapter = ConcatAdapter(myCountryAdapter, countryListAdapter)

        countryList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = concatAdapter
        }

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

                        val myCountry = countries.filter {
                            it.country.equals(countryName, ignoreCase = true)
                        }
                        myCountryAdapter.submitList(myCountry)

                        val filtered = countries.toMutableList()
                        filtered.removeAll(myCountry)

                        countryListAdapter.submitList(filtered)
                    }
                }
                is Resource.Error -> {
                    Timber.d(it.message)
                    loader.visibility(false)
                }
            }
        }

        setPerColumnSort()

    }

    private fun showCustomViewDialog(dialogBehavior: DialogBehavior = ModalDialog) {
        val dialog = MaterialDialog(this, dialogBehavior).show {
            title(R.string.filter)
            customView(R.layout.filter_view, scrollable = true, horizontalPadding = true)
            positiveButton(R.string.apply) { dialog ->
                handleBottomSheetTextFields(dialog.getCustomView())
                filterList()
                appliedFilters.text = filterText.toString()
            }
            negativeButton(R.string.reset) {
                val list = viewModel.originalCountryList.value ?: listOf()
                countryListAdapter.submitList(list)
                resetFilters()
                appliedFilters.text = filterText.toString()
            }
            lifecycleOwner(this@MainActivity)
            debugMode(false)
        }

        // Setup custom view content

        val customView = dialog.getCustomView()

        handleBottomSheetSpinners(customView)
    }

    private fun resetFilters() {
        filterText.clear()

        totalFilters.apply {
            value = ""
            condition = 0
        }

        recoveredFilters.apply {
            value = ""
            condition = 0
        }

        deathFilters.apply {
            value = ""
            condition = 0
        }
    }

    private fun filterList() {
        Timber.d("Total Filter ${totalFilters.condition} ${totalFilters.value}")
        Timber.d("Recovered Filter ${recoveredFilters.condition} ${recoveredFilters.value}")
        Timber.d("Death Filter ${deathFilters.condition} ${deathFilters.value}")

        val list = countryListAdapter.getList

        filterText.clear()
        filterText.append("Filtered by ")

        var filteredList = list.filter { country ->
            when (totalFilters.condition) {
                1 -> {
                    country.totalConfirmed.toInt() >= totalFilters.value.toInt()
                }
                2 -> {
                    country.totalConfirmed.toInt() <= totalFilters.value.toInt()
                }
                else -> {
                    country.totalConfirmed.toInt() > 0
                }
            }
        }

        filteredList = filteredList.filter { country ->
            when (recoveredFilters.condition) {
                1 -> {
                    country.totalRecovered.toInt() >= recoveredFilters.value.toInt()
                }
                2 -> {
                    country.totalRecovered.toInt() <= recoveredFilters.value.toInt()
                }
                else -> {
                    country.totalRecovered.toInt() > 0
                }
            }
        }

        filteredList = filteredList.filter { country ->
            when (deathFilters.condition) {
                1 -> {
                    country.totalDeaths.toInt() >= deathFilters.value.toInt()
                }
                2 -> {
                    country.totalDeaths.toInt() <= deathFilters.value.toInt()
                }
                else -> {
                    country.totalDeaths.toInt() > 0
                }
            }
        }

        when (totalFilters.condition) {
            1 -> filterText.append("Total Cases >=").append(totalFilters.value).appendln()
            2 -> filterText.append("Total Cases <=").append(totalFilters.value).appendln()
            else -> filterText.clear()
        }

        when (recoveredFilters.condition) {
            1 -> filterText.append("Recovered Cases >=").append(recoveredFilters.value).appendln()
            2 -> filterText.append("Recovered Cases <=").append(recoveredFilters.value).appendln()
            else -> filterText.clear()
        }

        when (deathFilters.condition) {
            1 -> filterText.append("Death Cases >=").append(deathFilters.value).appendln()
            2 -> filterText.append("Death Cases <=").append(deathFilters.value).appendln()
            else -> filterText.clear()
        }

        countryListAdapter.submitList(filteredList)
    }

    private fun handleBottomSheetTextFields(customView: View) {
        val totalValue = customView.total_text
        val recoveredValue = customView.recovered_text
        val deathValue = customView.deaths_text

        deathFilters.value = deathValue.text.toString()
        recoveredFilters.value = recoveredValue.text.toString()
        totalFilters.value = totalValue.text.toString()
    }

    private fun handleBottomSheetSpinners(customView: View) {

        val totalSpinner = customView.total_spinner
        val recoveredSpinner = customView.recovered_spinner
        val deathSpinner = customView.deaths_spinner
        val totalValue = customView.total_text
        val recoveredValue = customView.recovered_text
        val deathValue = customView.deaths_text

        totalValue.setText(totalFilters.value)
        recoveredValue.setText(recoveredFilters.value)
        deathValue.setText(deathFilters.value)

        totalSpinner.apply {
            val mAdapter = ArrayAdapter.createFromResource(
                this@MainActivity,
                R.array.filters,
                android.R.layout.simple_spinner_item
            )
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = mAdapter
        }

        totalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                totalFilters.condition = position
            }
        }

        totalSpinner.setSelection(totalFilters.condition)

        recoveredSpinner.apply {
            val mAdapter = ArrayAdapter.createFromResource(
                this@MainActivity,
                R.array.filters,
                android.R.layout.simple_spinner_item
            )
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = mAdapter
        }

        recoveredSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                recoveredFilters.condition = position
            }

        }

        recoveredSpinner.setSelection(recoveredFilters.condition)

        deathSpinner.apply {
            val mAdapter = ArrayAdapter.createFromResource(
                this@MainActivity,
                R.array.filters,
                android.R.layout.simple_spinner_item
            )
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter = mAdapter
        }

        deathSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                deathFilters.condition = position
            }
        }

        deathSpinner.setSelection(deathFilters.condition)

    }

    private fun setPerColumnSort() {

        total.setOnClickListener {
            val list = countryListAdapter.getList

            if (totalDesc) {
                totalDesc = false
                total.drawableEnd(R.drawable.ic_down)
                countryListAdapter.submitList(list.sortedBy { it.totalConfirmed.toInt() })
            } else {
                totalDesc = true
                total.drawableEnd(R.drawable.ic_up)
                countryListAdapter.submitList(list.sortedByDescending { it.totalConfirmed.toInt() })
            }
        }

        recovered.setOnClickListener {
            val list = countryListAdapter.getList

            if (recoveredDesc) {
                recoveredDesc = false
                recovered.drawableEnd(R.drawable.ic_down)
                countryListAdapter.submitList(list.sortedBy { it.totalRecovered.toInt() })
            } else {
                recoveredDesc = true
                recovered.drawableEnd(R.drawable.ic_up)
                countryListAdapter.submitList(list.sortedByDescending { it.totalRecovered.toInt() })
            }
        }


        deaths.setOnClickListener {
            val list = countryListAdapter.getList

            if (deathsDesc) {
                deathsDesc = false
                deaths.drawableEnd(R.drawable.ic_down)
                countryListAdapter.submitList(list.sortedBy { it.totalDeaths.toInt() })
            } else {
                deathsDesc = true
                deaths.drawableEnd(R.drawable.ic_up)
                countryListAdapter.submitList(list.sortedByDescending { it.totalDeaths.toInt() })
            }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_filter, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.filter) {
            showCustomViewDialog(BottomSheet(LayoutMode.WRAP_CONTENT))
            true
        } else
            super.onOptionsItemSelected(item)
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

}