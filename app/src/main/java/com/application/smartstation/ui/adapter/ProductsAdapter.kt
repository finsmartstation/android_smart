package com.application.smartstation.ui.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.*
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ProductsAdapter(val context: Context) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {

    private var list = emptyList<ProductCateListRes>()
    var onItemClick: ((model: ProductCateListRes) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.productQut.text = model.productQty
            binding.prodName.text = model.productName
            binding.prodPrice.text = model.productPrice

            Glide.with(context).load(model.imageUrl).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.productImage)


//            itemView.setOnClickListener {
//                onItemClick!!.invoke(model)
//            }
        }
    }

    internal fun setCloud(cloud: List<ProductCateListRes>) {
        this.list = cloud
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemProductBinding.bind(itemView)
    }
}