package com.application.smartstation.ui.adapter

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemInboxBinding
import com.application.smartstation.ui.model.SendMailListRes
import com.application.smartstation.util.UtilsDefault


class SentboxAdapter(val context: Context) : RecyclerView.Adapter<SentboxAdapter.ViewHolder>() {

    private var list = emptyList<SendMailListRes>()
    var onItemClick: ((model: SendMailListRes) -> Unit)? = null

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
            val str: String = TextUtils.join(",", model.to)
            binding.txtTitle.text = str
            binding.txtDate.text = UtilsDefault.dateMail(UtilsDefault.localTimeConvert(model.createdAt)!!)
            binding.txtTime.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.createdAt))
            binding.txtSub.text = model.subject
            binding.txtBody.text = Html.fromHtml(model.body)

            if (model.attachments != null){
                if (!model.attachments.isNullOrEmpty()){
                    binding.imgAttach.visibility = View.VISIBLE
                }else{
                    binding.imgAttach.visibility = View.GONE
                }
            }else{
                binding.imgAttach.visibility = View.GONE
            }

//            Glide.with(context).load(model.profile_pic).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgMailProfile)

//            if (model.mail_read_status == "1"){
                binding.txtTitle.setTextColor(context.resources.getColor(R.color.black))
//            }else{
//                binding.txtTitle.setTextColor(context.resources.getColor(R.color.mail_date_coloe))
//            }

            itemView.setOnClickListener {
                onItemClick!!.invoke(model)
            }

        }
    }

    internal fun setMail(mail: List<SendMailListRes>) {
        this.list = mail
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemInboxBinding.bind(itemView)
    }
}