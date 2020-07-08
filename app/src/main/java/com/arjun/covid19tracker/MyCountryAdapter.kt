package com.arjun.covid19tracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arjun.covid19tracker.model.Country
import kotlinx.android.synthetic.main.item_my_country.view.*

class MyCountryAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Country>() {

        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return CountryListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_my_country,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CountryListViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Country>) {
        differ.submitList(list)
    }

    class CountryListViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        val card = itemView.card_state

        val country = itemView.country
        val total = itemView.total
        val deaths = itemView.deaths
        val recovered = itemView.recovered

        fun bind(item: Country) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            country.text = item.country
            total.text = item.totalConfirmed.toString()
            deaths.text = item.totalDeaths.toString()
            recovered.text = item.totalRecovered.toString()

            // Alternating row colors
            if ((adapterPosition + 1) % 2 == 0) {
                card.setBackgroundColor(
                    ContextCompat.getColor(
                        card.context,
                        R.color.grayBgLight
                    )
                )
                total.setBackgroundColor(
                    ContextCompat.getColor(
                        total.context,
                        R.color.grayBgLight
                    )
                )
                deaths.setBackgroundColor(
                    ContextCompat.getColor(
                        total.context,
                        R.color.grayBgLight
                    )
                )
                recovered.setBackgroundColor(
                    ContextCompat.getColor(
                        total.context,
                        R.color.grayBgLight
                    )
                )
            } else {
                card.setBackgroundColor(Color.TRANSPARENT)
                total.setBackgroundColor(Color.TRANSPARENT)
                deaths.setBackgroundColor(Color.TRANSPARENT)
                recovered.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Country)
    }
}

