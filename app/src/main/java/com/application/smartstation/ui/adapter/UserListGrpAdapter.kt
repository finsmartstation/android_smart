package com.application.smartstation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemUserListGrpBinding
import com.application.smartstation.ui.model.UserListGrp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class UserListGrpAdapter(val context: Context) :
    RecyclerView.Adapter<UserListGrpAdapter.ViewHolder>() {

    private var list = emptyList<UserListGrp>()
    var onItemClick: ((model: UserListGrp) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_list_grp, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            binding.txtTitle.text = model.username
            binding.txtSub.text = model.about
            Glide.with(context).load(model.profile_pic).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.imgProfile)

            if (model.type == "admin") {
                binding.txtType.visibility = View.VISIBLE
                binding.txtType.text = "Admin"
            } else {
                binding.txtType.visibility = View.GONE
            }

        }
    }

    internal fun setUser(user: List<UserListGrp>) {
        this.list = user
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemUserListGrpBinding.bind(itemView)
    }
}