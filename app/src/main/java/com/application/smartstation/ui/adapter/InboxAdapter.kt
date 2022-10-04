package com.application.smartstation.ui.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemInboxBinding
import com.application.smartstation.ui.model.DataMailList
import com.application.smartstation.util.UtilsDefault
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
            if (model.type.equals("date")){
                binding.txtDate.visibility = View.VISIBLE
                binding.llLetterView.visibility = View.GONE
                binding.txtDate.text =
                   UtilsDefault.dateMail(UtilsDefault.localTimeConvert(model.createdAt)!!)
            }else {
                binding.txtDate.visibility = View.GONE
                binding.llLetterView.visibility = View.VISIBLE
                binding.txtTitle.text = model.from
                binding.txtTime.text =
                    UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.createdAt))
                binding.txtSub.text = model.subject
                binding.txtBody.text = Html.fromHtml(model.body)

                if (model.attachments != null) {
                    if (!model.attachments.isNullOrEmpty()) {
                        binding.imgAttach.visibility = View.VISIBLE
                    } else {
                        binding.imgAttach.visibility = View.GONE
                    }
                } else {
                    binding.imgAttach.visibility = View.GONE
                }

                Glide.with(context).load(model.profile_pic).placeholder(R.drawable.ic_default)
                    .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.imgMailProfile)

                if (model.mail_read_status == "0") {
                    binding.txtSub.setTextColor(context.resources.getColor(R.color.color_chat_popup_gray))
                    binding.txtBody.setTextColor(context.resources.getColor(R.color.color_chat_popup_gray))
                } else {
                    binding.txtSub.setTextColor(context.resources.getColor(R.color.black))
                    binding.txtBody.setTextColor(context.resources.getColor(R.color.black))
                }

                itemView.setOnClickListener {
                    onItemClick!!.invoke(model)
                }
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