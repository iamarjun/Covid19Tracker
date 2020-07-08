package com.arjun.covid19tracker

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arjun.covid19tracker.databinding.ActivityMainBinding
import com.arjun.covid19tracker.model.Resource
import com.arjun.covid19tracker.util.viewbinding.viewBinding
import com.arjun.covid19tracker.util.visibility
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    private lateinit var confirmedValue: TextView
    private lateinit var recoveredValue: TextView
    private lateinit var deceasedValue: TextView
    private lateinit var loader: ProgressBar
    private lateinit var countryList: RecyclerView
    private lateinit var viewModel: MainViewModel
    private val countryListAdapter by lazy { CountryListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        confirmedValue = binding.textConfirmedValue
        recoveredValue = binding.textRecoveredValue
        deceasedValue = binding.textDeceasedValue
        loader = binding.loader
        countryList = binding.countryList

        countryList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = countryListAdapter
        }

        viewModel.getLatestCovidUpdates()
    }

    override fun onResume() {
        super.onResume()

        viewModel.covidData.observe(this) {
            when (it) {

                is Resource.Loading -> {
                    Timber.d(it.message)
                    loader.visibility(true)

                }
                is Resource.Success -> {
                    Timber.d(it.data.toString())
                    loader.visibility(false)
                    it.data?.let { covid ->
                        countryListAdapter.submitList(covid.countries)
                    }
                }
                is Resource.Error -> {
                    Timber.d(it.message)
                    loader.visibility(false)
                }
            }
        }
    }
}