package com.application.smartstation.ui.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.databinding.ItemInboxBinding
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.DataMailList
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class InboxAdapter(val context: Context) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private var list = emptyList<DataMailList>()
    var onItemClick: ((model: DataMailList) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inbox, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtTitle.text = model.subject
            binding.txtDate.text = UtilsDefault.dateConvert(model.datetime)
            binding.txtTime.text = UtilsDefault.todayDate(model.datetime)
            binding.txtSub.text = model.body
            Glide.with(context).load(model.profile_pic).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgMailProfile)

            if (model.mail_read_status == "1"){
                binding.txtTitle.setTextColor(context.resources.getColor(R.color.black))
            }else{
                binding.txtTitle.setTextColor(context.resources.getColor(R.color.mail_date_coloe))
            }

        }
    }

    internal fun setMail(mail: List<DataMailList>) {
        this.list = mail
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemInboxBinding.bind(itemView)
    }
}