package com.application.smartstation.ui.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.databinding.ItemGrpChatBinding
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ContactGroupAdapter(val context: Context) : RecyclerView.Adapter<ContactGroupAdapter.ViewHolder>() {

    private var list = emptyList<DataUserList>()
    var onItemClick: ((model: DataUserList,pos:Int) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_grp_chat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtName.text = model.name
            binding.txtMsg.text = model.about

            Glide.with(context).load(model.profile_pic).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgProfile)

            binding.flChat.setOnClickListener {
                onItemClick!!.invoke(model,position)
            }

            if (model.statusSelected){
                binding.imgTick.visibility = View.VISIBLE
            }else{
                binding.imgTick.visibility = View.GONE
            }

            Log.d("TAG", "onBindViewHolder: "+model.statusSelected)

        }
    }

    internal fun setChat(chat: List<DataUserList>) {
        this.list = chat
        notifyDataSetChanged()
    }

    internal fun setChats(position: Int) {
        notifyItemChanged(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemGrpChatBinding.bind(itemView)
    }
}