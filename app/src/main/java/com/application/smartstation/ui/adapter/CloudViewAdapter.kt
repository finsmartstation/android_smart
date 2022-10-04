package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemCloudViewBinding
import com.application.smartstation.ui.model.CloudDetailListRes
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

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_cloud_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {

            if (model.file_type.equals("folder")) {
                binding.llView.visibility = View.VISIBLE
                binding.clView.visibility = View.GONE
                binding.txtFolderName.text = model.name

                binding.txtFolderDate.text = UtilsDefault.dateConvert(model.created_datetime)

                if (model.view_type.equals("life_time")){
                    binding.txtFolderDateExp.visibility = View.GONE
                }else{
                    binding.txtFolderDateExp.visibility = View.VISIBLE
                    binding.txtFolderDateExp.text = "Exp : "+UtilsDefault.dateConvert(model.end_datetime)+" "+UtilsDefault.todayDate(model.end_datetime)
                }

            } else {
                val fileName = FileUtils.getFileNameFromPath(model.file_path).replace("/", "")
                var type = ""
                if (UtilsDefault.isImageFile(model.file_path)) {
                    type = "img"
                } else if (UtilsDefault.isPdfFile(model.file_path)) {
                    type = "pdf"
                } else {
                    type = ""
                }

                if (model.view_type.equals("life_time")){
                    binding.txtDateExp.visibility = View.GONE
                }else{
                    binding.txtDateExp.visibility = View.VISIBLE
                    binding.txtDateExp.text = "Exp : "+UtilsDefault.dateConvert(model.end_datetime)+" "+UtilsDefault.todayDate(model.end_datetime)
                }

                binding.txtDate.text = UtilsDefault.dateConvert(model.created_datetime)

                binding.llView.visibility = View.GONE
                binding.clView.visibility = View.VISIBLE
                if (type.equals("img")) {
                    binding.llFileName.visibility = View.GONE
                    Glide.with(context).load(model.file_path).placeholder(R.drawable.ic_default)
                        .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.imgPath)
                } else if (type.equals("pdf")) {
                    binding.llFileName.visibility = View.VISIBLE
                    binding.txtFileName.text = fileName
                    Glide.with(context).load(R.drawable.pdf_icon).placeholder(R.drawable.ic_default)
                        .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(binding.imgPath)
                } else {
                    binding.llFileName.visibility = View.GONE
                }
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