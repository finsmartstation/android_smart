package com.application.smartstation.ui.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatBinding
import com.application.smartstation.databinding.ItemChatHisBinding
import com.application.smartstation.ui.model.ChatDetailsRes
import com.application.smartstation.ui.model.ChatHisResponse
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.ui.model.DataResChat
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ChatHistoryAdapter(val context: Context) : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    private var list = emptyList<ChatDetailsRes>()
    var onItemClick: ((pos: Int) -> Unit)? = null
    var blinkItem = NO_POSITION
    var chatType = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_his, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        with(holder) {
            if (model.type.equals("date")) {
                binding.txtDate.text = UtilsDefault.formatToYesterdayOrToday(model.date)
                binding.llChatDate.visibility = View.VISIBLE
                binding.llChatMsg.visibility = View.GONE
            }
            else if (model.type.equals("notification")){
                binding.txtDate.text = model.message
                binding.llChatDate.visibility = View.VISIBLE
                binding.llChatMsg.visibility = View.GONE
            }
            else {
                binding.llChatDate.visibility = View.GONE
                binding.llChatMsg.visibility = View.VISIBLE
                if (model.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))){
                    model.type = "sent"
                }else{
                    model.type = "receive"
                }
                if (model.type.equals("receive")) {

                    if (chatType){
                        binding.txtRecName.visibility = View.VISIBLE
                        binding.txtRecName.text = model.name
                    }else{
                        binding.txtRecName.visibility = View.GONE
                    }

                    binding.llRec.visibility = View.VISIBLE
                    binding.llSend.visibility = View.GONE
                    if (model.message_type.equals("text")) {
                        binding.flMsg.visibility = View.VISIBLE
                        binding.imgChat.visibility = View.GONE
                        binding.txtTimeImg.visibility = View.GONE
                        binding.txtMsg.text =model.message
                    } else {
                        binding.flMsg.visibility = View.GONE
                        binding.imgChat.visibility = View.VISIBLE
                        binding.txtTimeImg.visibility = View.VISIBLE
                        Glide.with(context).load(model.message)
                            .diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).into(binding.imgChat)
                    }
                    binding.txtTime.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    binding.txtTimeImg.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                } else {
                    if (model.message_status.equals("0")) {
                        binding.llSend.setBackgroundResource(R.drawable.send_chat_bg_blue)
                        binding.llRec.visibility = View.GONE
                        binding.llSend.visibility = View.VISIBLE
                        if (model.message_type.equals("text")) {
                            binding.flMsgSend.visibility = View.VISIBLE
                            binding.imgChatSend.visibility = View.GONE
                            binding.txtTimeImgSend.visibility = View.GONE
                            binding.txtMsgSend.text =model.message
                        } else {
                            binding.flMsgSend.visibility = View.GONE
                            binding.imgChatSend.visibility = View.VISIBLE
                            binding.txtTimeImgSend.visibility = View.VISIBLE
                            Glide.with(context).load(model.message).placeholder(R.drawable.ic_default).error(R.drawable.ic_default)
                                .diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgChatSend)
                        }
                        binding.txtTimeSend.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                        binding.txtTimeImgSend.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    } else {
                        binding.llSend.setBackgroundResource(R.drawable.send_chat_bg_green)
                        binding.llRec.visibility = View.GONE
                        binding.llSend.visibility = View.VISIBLE
                        if (model.message_type.equals("text")) {
                            binding.flMsgSend.visibility = View.VISIBLE
                            binding.imgChatSend.visibility = View.GONE
                            binding.txtTimeImgSend.visibility = View.GONE
                            binding.txtMsgSend.text = model.message
                        } else {
                            binding.flMsgSend.visibility = View.GONE
                            binding.imgChatSend.visibility = View.VISIBLE
                            binding.txtTimeImgSend.visibility = View.VISIBLE
                            Glide.with(context).load(model.message).placeholder(R.drawable.ic_default).error(R.drawable.ic_default)
                                .diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgChatSend)
                        }
                        binding.txtTimeSend.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                        binding.txtTimeImgSend.text = UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }
                }
            }

            binding.replyLayoutRec.setOnClickListener {
                onItemClick!!.invoke(position)
            }

            binding.replyLayoutSend.setOnClickListener {
                onItemClick!!.invoke(position)
            }

//            itemView.setOnClickListener {
//                onItemClick!!.invoke(1)
//            }

            if (blinkItem == position) {
                val anim: Animation = AlphaAnimation(0.0f, 0.5f)
                android.os.Handler().postDelayed({
                    anim.duration = 200
                    itemView.startAnimation(anim)
                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            blinkItem = NO_POSITION
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                }, 100)

            }

        }
    }

    internal fun setChatHis(chat: List<ChatDetailsRes>,chatType:Boolean) {
        this.list = chat
        this.chatType = chatType
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatHisBinding.bind(itemView)

    }

    fun blinkItem(position: Int) {
        blinkItem = position
        notifyItemChanged(position)
    }
}