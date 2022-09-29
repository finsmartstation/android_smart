package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemCloudBinding
import com.application.smartstation.ui.model.CloudListRes
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class CloudAdapter(val context: Context) : RecyclerView.Adapter<CloudAdapter.ViewHolder>() {

    private var list = emptyList<CloudListRes>()
    var onItemClick: ((model: CloudListRes) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloud, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtNum.text = model.phone
            Glide.with(context).load(R.drawable.ic_default).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default)
                .into(binding.imgProfile)

            itemView.setOnClickListener {
                onItemClick!!.invoke(model)
            }
        }
    }

    internal fun setCloud(cloud: List<CloudListRes>) {
        this.list = cloud
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemCloudBinding.bind(itemView)
    }
}