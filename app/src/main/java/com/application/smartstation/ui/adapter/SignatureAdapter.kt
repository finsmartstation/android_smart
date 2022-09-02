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
import com.application.smartstation.databinding.ItemSignatureBinding
import com.application.smartstation.ui.model.SendMailListRes
import com.application.smartstation.ui.model.SignatureListData
import com.application.smartstation.ui.model.StampList
import com.application.smartstation.util.UtilsDefault
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class SignatureAdapter(val context: Context,val type:Int) : RecyclerView.Adapter<SignatureAdapter.ViewHolder>() {

    private var list = emptyList<SignatureListData>()
    var onItemDeleteClick: ((model: SignatureListData) -> Unit)? = null
    var onItemClick: ((model: SignatureListData) -> Unit)? = null
    var onItemSelectClick: ((model: SignatureListData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_signature, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtTitle.text = model.name
            Glide.with(context).load(model.image).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA).into(binding.imgSignature)

            if (model.default){
                binding.txtPrimary.visibility = View.VISIBLE
                binding.txtSetPrimary.visibility = View.GONE
            }else{
                binding.txtPrimary.visibility = View.GONE
                binding.txtSetPrimary.visibility = View.VISIBLE
            }

            if (type.equals(2)){
                binding.imgDelete.visibility = View.GONE
                binding.txtSetPrimary.visibility = View.GONE
            }else{
                binding.imgDelete.visibility = View.VISIBLE
                binding.txtSetPrimary.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                onItemSelectClick!!.invoke(model)
            }

            binding.imgDelete.setOnClickListener {
                onItemDeleteClick!!.invoke(model)
            }

            binding.txtSetPrimary.setOnClickListener {
                onItemClick!!.invoke(model)
            }

        }
    }

    internal fun setSignature(signature: List<SignatureListData>) {
        this.list = signature
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSignatureBinding.bind(itemView)
    }
}