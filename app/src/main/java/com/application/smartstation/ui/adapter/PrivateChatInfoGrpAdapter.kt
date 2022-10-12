package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.ui.model.CommonGrpList
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class PrivateChatInfoGrpAdapter(val context: Context) : RecyclerView.Adapter<PrivateChatInfoGrpAdapter.ViewHolder>() {

    private var list = emptyList<CommonGrpList>()
    var onItemClick: ((model: CommonGrpList) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtName.text = model.group_name
            binding.txtMsg.text = model.group_users
            binding.txtTime.visibility = View.GONE

            Glide.with(context).load(model.group_profile_pic).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default)
                .into(binding.imgProfile)
            binding.llRead.visibility = View.GONE

            binding.flChat.setOnClickListener {
                onItemClick!!.invoke(model)
            }

        }
    }

    internal fun setChat(chat: List<CommonGrpList>) {
        this.list = chat
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatBinding.bind(itemView)
    }
}