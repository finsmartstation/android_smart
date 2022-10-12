package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class ChatAdapter(val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private var list = emptyList<DataChatList>()
    var onItemClick: ((model: DataChatList) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()
    var typingStatus = 0

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
            binding.txtName.text = model.name
            binding.txtTime.text =
                UtilsDefault.dateChatList(UtilsDefault.localTimeConvert(model.date))

            Glide.with(context).load(model.profile).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.imgProfile)

            if (typingStatus.equals(1)) {
                binding.llView.visibility = View.GONE
                binding.txtTyping.visibility = View.VISIBLE
            } else {
                binding.llView.visibility = View.VISIBLE
                binding.txtTyping.visibility = View.GONE

            }


            binding.flChat.setOnClickListener {
                onItemClick!!.invoke(model)
            }

            if (model.unread_message.equals("0")) {
                binding.llRead.visibility = View.GONE
                if (!model.message_type.equals("")) {
                    if (model.message_type.equals("text")) {
                        binding.txtMsg.text = model.message
                        binding.imgPht.visibility = View.GONE
                    } else {
                        binding.txtMsg.text = "Photo"
                        binding.imgPht.visibility = View.VISIBLE
                    }
                    binding.txtMsg.setTextColor(context.resources.getColor(R.color.color_gray_2))
                } else {
                    binding.txtMsg.text = model.message
                }
            } else {
                binding.llRead.visibility = View.VISIBLE
                if (model.unread_message > "9") {
                    binding.txtUnread.text = "9+"
                } else {
                    binding.txtUnread.text = model.unread_message
                }
                if (!model.message_type.equals("")) {
                    if (model.message_type.equals(Constants.TEXT)) {
                        binding.txtMsg.text = UtilsDefault.chatBold(model.message)
                        binding.imgPht.visibility = View.GONE
                    } else {
                        binding.txtMsg.text = "Photo"
                        binding.imgPht.visibility = View.VISIBLE
                    }
                    binding.txtMsg.setTextColor(context.resources.getColor(R.color.black))
                } else {
                    binding.txtMsg.text = UtilsDefault.chatBold(model.message)
                }
            }

        }
    }

    internal fun setChat(chat: List<DataChatList>) {
        this.list = chat
        notifyDataSetChanged()
    }

    internal fun setChats(typing: Int, position: Int) {
        this.typingStatus = typing
        notifyItemChanged(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatBinding.bind(itemView)
    }
}