package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemInboxBinding
import com.application.smartstation.databinding.ItemReceivedLetterBinding
import com.application.smartstation.ui.model.ChatResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ReceivedLetterAdapter(val context: Context) : RecyclerView.Adapter<ReceivedLetterAdapter.ViewHolder>() {

    private var list = emptyList<ChatResponse>()
    var onItemClick: ((model: ChatResponse) -> Unit)? = null

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
            binding.txtTitle.text = model.title
            binding.txtDate.text = model.date
            binding.txtTime.text = model.time
            binding.txtSub.text = model.sub
            Glide.with(context).load(model.profile).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgMailProfile)

            if (model.status == 1){
                binding.txtTitle.setTextColor(context.resources.getColor(R.color.black))
            }else{
                binding.txtTitle.setTextColor(context.resources.getColor(R.color.mail_date_coloe))
            }
        }
    }

    internal fun setReceivedLetter(letter: List<ChatResponse>) {
        this.list = letter
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemInboxBinding.bind(itemView)

    }
}