package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemCountryBinding
import com.application.smartstation.ui.model.CountryList
import com.application.smartstation.util.UtilsDefault

class SelectCountryAdapter(val context: Context) : RecyclerView.Adapter<SelectCountryAdapter.ViewHolder>() {

    private var list = emptyList<CountryList>()
    var onItemClick: ((model: CountryList) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {

            binding.txtCountry.setText(UtilsDefault.countryImg(model.countryCode.toUpperCase()))
            binding.txtCountryCode.text = "+ "+model.countryNum
            binding.txtCountryName.text = model.countryName

            itemView.setOnClickListener {
                onItemClick!!.invoke(model)
            }
        }
    }

    internal fun setCountry(country: List<CountryList>) {
        this.list = country
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemCountryBinding.bind(itemView)

    }


}