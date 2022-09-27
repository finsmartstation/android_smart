package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemProductCategoryBinding
import com.application.smartstation.ui.model.ProductCateListRes
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class CategoryAdapter(val context: Context) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var list = emptyList<ProductCateListRes>()
    var onItemClick: ((model: ProductCateListRes) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_category, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtCategory.text = model.category_name
            Glide.with(context).load(model.category_image).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.imgCategory)

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
        val binding = ItemProductCategoryBinding.bind(itemView)
    }
}