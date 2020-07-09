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
import kotlinx.android.synthetic.main.item_header.view.*

class HeaderAdapter(private val interaction: SortInteraction? = null) :
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
                R.layout.item_header,
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
        private val interaction: SortInteraction?
    ) : RecyclerView.ViewHolder(itemView) {

        private val card = itemView.card_state

        private val country = itemView.country
        private val total = itemView.total
        private val deaths = itemView.deaths
        private val recovered = itemView.recovered

        fun bind(item: Country) {

            country.text = item.country
            total.text = item.totalConfirmed
            deaths.text = item.totalDeaths
            recovered.text = item.totalRecovered

            total.setOnClickListener { interaction?.sortByTotalCases() }
            deaths.setOnClickListener { interaction?.sortByDeaths() }
            recovered.setOnClickListener { interaction?.sortByRecovered() }

            // Alternating row colors
            if ((bindingAdapterPosition + 1) % 2 == 0) {
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

    interface SortInteraction {
        fun sortByTotalCases()
        fun sortByRecovered()
        fun sortByDeaths()
    }
}

