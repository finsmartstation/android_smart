package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemInboxBinding
import com.application.smartstation.databinding.ItemMailFileBinding
import com.application.smartstation.ui.model.DataMailList
import com.application.smartstation.ui.model.MailImageSelect
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File


class MailFilesAdapter(val context: Context) : RecyclerView.Adapter<MailFilesAdapter.ViewHolder>() {

    private var list = emptyList<MailImageSelect>()
    var onItemClick: ((model: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mail_file, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            val fileName = FileUtils.getFileNameFromPath(model.imagePath).replace("/","")
            if (model.type.equals("img")){
                binding.llFileName.visibility = View.GONE
                Glide.with(context).load(model.imagePath).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgPath)
            }else if (model.type.equals("pdf")){
                binding.llFileName.visibility = View.VISIBLE
                binding.txtFileName.text = fileName
                Glide.with(context).load(R.drawable.pdf_icon).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgPath)
            }else{
                binding.llFileName.visibility = View.GONE
            }

            binding.imgClose.setOnClickListener {
                onItemClick!!.invoke(position)
            }

        }
    }

    internal fun setMail(mail: List<MailImageSelect>) {
        this.list = mail
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemMailFileBinding.bind(itemView)
    }
}