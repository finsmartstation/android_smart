package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemAddGrpChatBinding
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class AddGroupAdapter(val context: Context) : RecyclerView.Adapter<AddGroupAdapter.ViewHolder>() {

    private var list = emptyList<DataUserList>()
    var onItemClick: ((model: DataUserList, pos: Int) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()
    var showClose = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_grp_chat, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtName.text = model.name

            Glide.with(context).load(model.profile_pic).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.imgProfile)

            binding.imgClose.setOnClickListener {
                onItemClick!!.invoke(model, position)
            }

            if (!showClose) {
                binding.imgClose.visibility = View.GONE
            } else {
                binding.imgClose.visibility = View.VISIBLE
            }

        }
    }

    internal fun setChat(chat: List<DataUserList>, close: Boolean) {
        this.list = chat
        this.showClose = close
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemAddGrpChatBinding.bind(itemView)
    }
}