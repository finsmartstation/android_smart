package com.application.smartstation.ui.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.databinding.ItemCloudBinding
import com.application.smartstation.databinding.ItemCloudViewBinding
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class CloudViewAdapter(val context: Context) : RecyclerView.Adapter<CloudViewAdapter.ViewHolder>() {

    private var list = emptyList<CloudDetailListRes>()
    var onItemClick: ((model: CloudDetailListRes) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloud_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            val fileName = FileUtils.getFileNameFromPath(model.filepath).replace("/","")
            if (UtilsDefault.isImageFile(model.filepath)){
                model.type = "img"
            }else if (UtilsDefault.isPdfFile(model.filepath)){
                model.type = "pdf"
            }else{
                model.type = ""
            }
            if (model.type.equals("img")){
                binding.llFileName.visibility = View.GONE
                Glide.with(context).load(model.filepath).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgPath)
            }else if (model.type.equals("pdf")){
                binding.llFileName.visibility = View.VISIBLE
                binding.txtFileName.text = fileName
                Glide.with(context).load(R.drawable.pdf_icon).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgPath)
            }else{
                binding.llFileName.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClick!!.invoke(model)
            }
        }
    }

    internal fun setCloud(cloud: List<CloudDetailListRes>) {
        this.list = cloud
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemCloudViewBinding.bind(itemView)
    }
}