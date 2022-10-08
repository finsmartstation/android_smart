package com.application.smartstation.ui.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ItemChatNewBinding
import com.application.smartstation.ui.model.ChatDetailsRes
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.view.ViewBinderHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class AdapterChat(val context: Context) : RecyclerView.Adapter<AdapterChat.ViewHolder>() {

    private var list = emptyList<ChatDetailsRes>()
    var onItemClick: ((model: ChatDetailsRes) -> Unit)? = null
    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_new, parent, false)
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
                binding.icSender.main.visibility = View.GONE
                binding.icReceiver.main.visibility = View.GONE
            }else if (model.type.equals("notification")) {
                binding.txtDate.text = model.message
                binding.llChatDate.visibility = View.VISIBLE
                binding.icSender.main.visibility = View.GONE
                binding.icReceiver.main.visibility = View.GONE
            }else {
                binding.llChatDate.visibility = View.GONE

                if (model.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
                    model.type = "sent"
                } else {
                    model.type = "receive"
                }

                //rec
                if (model.type.equals("receive")){
                    binding.icSender.main.visibility = View.GONE
                    binding.icReceiver.main.visibility = View.VISIBLE

                    //txt
                    if (model.message_type.equals(Constants.TEXT)){
                        binding.icReceiver.txtMsg.visibility = View.VISIBLE
                        binding.icReceiver.txtTime.visibility = View.VISIBLE
                        binding.icReceiver.mediaLayout.visibility = View.GONE
                        binding.icReceiver.doc.visibility = View.GONE
                        binding.icReceiver.txtTime1.visibility = View.GONE
                        binding.icReceiver.voicePlayerView.visibility = View.GONE

                        binding.icReceiver.txtMsg.setLinkText(model.message)
                        binding.icReceiver.txtTime.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                        binding.icReceiver.txtMsg.setOnLinkClickListener { i, s ->
                            if (i == 16) {
                                var ss = s
                                if (!s.startsWith("https://") && !s.startsWith("http://")) {
                                    ss = "http://$s"
                                }
                                val openUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ss))
                                context.startActivity(openUrlIntent)
                            } else if (i == 4) {
                                val intent =
                                    Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null))
                                context.startActivity(intent)
                            } else if (i == 8) {
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:")
                                intent.putExtra(Intent.EXTRA_EMAIL, s)
                                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                                context.startActivity(intent)
                            }
                        }
                    }

                    //img
                    else if (model.message_type.equals(Constants.IMAGE)){

                        binding.icReceiver.mediaLayout.visibility = View.VISIBLE
                        binding.icReceiver.media.visibility = View.VISIBLE
                        binding.icReceiver.txtMsg.visibility = View.GONE
                        binding.icReceiver.txtTime.visibility = View.GONE
                        binding.icReceiver.doc.visibility = View.GONE
                        binding.icReceiver.txtTime1.visibility = View.VISIBLE
                        binding.icReceiver.voicePlayerView.visibility = View.GONE

                        Glide.with(context).load(model.message).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.icReceiver.media)
                        binding.icReceiver.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //video
                    else if (model.message_type.equals(Constants.VIDEO)){
                        binding.icReceiver.txtMsg.visibility = View.GONE
                        binding.icReceiver.txtTime.visibility = View.GONE
                        binding.icReceiver.doc.visibility = View.GONE
                        binding.icReceiver.mediaLayout.visibility = View.VISIBLE
                        binding.icReceiver.media.visibility = View.VISIBLE
                        binding.icReceiver.play.visibility = View.VISIBLE
                        binding.icReceiver.txtTime1.visibility = View.VISIBLE
                        binding.icReceiver.voicePlayerView.visibility = View.GONE

                        Glide.with(context).asBitmap().load(model.message).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .thumbnail(0.1f).into(binding.icReceiver.media)
                        binding.icReceiver.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //audio

                    else if (model.message_type.equals(Constants.AUDIO)){

                        binding.icReceiver.mediaLayout.visibility = View.VISIBLE
                        binding.icReceiver.voicePlayerView.visibility = View.VISIBLE
                        binding.icReceiver.txtTime.visibility = View.GONE
                        binding.icReceiver.media.visibility = View.GONE
                        binding.icReceiver.doc.visibility = View.GONE
                        binding.icReceiver.play.visibility = View.GONE
                        binding.icReceiver.txtMsg.visibility = View.GONE
                        binding.icReceiver.txtTime1.visibility = View.VISIBLE

                        binding.icReceiver.voicePlayerView.setAudio(model.message)
                        binding.icReceiver.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //docs
                    else if (model.message_type.equals(Constants.DOCS) || model.message_type.equals(Constants.PDF)){
                        binding.icReceiver.mediaLayout.visibility = View.VISIBLE
                        binding.icReceiver.doc.visibility = View.VISIBLE
                        binding.icReceiver.play.visibility = View.GONE
                        binding.icReceiver.txtTime1.visibility = View.VISIBLE
                        binding.icReceiver.voicePlayerView.visibility = View.GONE
                        binding.icReceiver.txtMsg.visibility = View.GONE
                        binding.icReceiver.media.visibility = View.GONE
                        binding.icReceiver.txtTime.visibility = View.GONE

                        binding.icReceiver.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //location
                    else if (model.message_type.equals(Constants.LOCATION)){

                    }

                }
                else{
                    binding.icSender.main.visibility = View.VISIBLE
                    binding.icReceiver.main.visibility = View.GONE

                    if (model.message_status.equals("0")) {
                        binding.icSender.main.setBackgroundResource(R.drawable.send_chat_bg_blue)
                    }else{
                        binding.icSender.main.setBackgroundResource(R.drawable.message_bg)
                    }

                    //txt
                    if (model.message_type.equals(Constants.TEXT)){
                        binding.icSender.txtMsg.visibility = View.VISIBLE
                        binding.icSender.mediaLayout.visibility = View.GONE
                        binding.icSender.txtTime.visibility = View.VISIBLE
                        binding.icSender.txtTime1.visibility = View.GONE
                        binding.icSender.doc.visibility = View.GONE
                        binding.icSender.voicePlayerView.visibility = View.GONE

                        binding.icSender.txtMsg.setLinkText(model.message)
                        binding.icSender.txtTime.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                        binding.icSender.txtMsg.setOnLinkClickListener { i, s ->
                            if (i == 16) {
                                var ss = s
                                if (!s.startsWith("https://") && !s.startsWith("http://")) {
                                    ss = "http://$s"
                                }
                                val openUrlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ss))
                                context.startActivity(openUrlIntent)
                            } else if (i == 4) {
                                val intent =
                                    Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null))
                                context.startActivity(intent)
                            } else if (i == 8) {
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("mailto:")
                                intent.putExtra(Intent.EXTRA_EMAIL, s)
                                intent.putExtra(Intent.EXTRA_SUBJECT, "")
                                context.startActivity(intent)
                            }
                        }
                    }

                    //img
                    else if (model.message_type.equals(Constants.IMAGE)){
                        binding.icSender.mediaLayout.visibility = View.VISIBLE
                        binding.icSender.media.visibility = View.VISIBLE
                        binding.icSender.txtMsg.visibility = View.GONE
                        binding.icSender.doc.visibility = View.GONE
                        binding.icSender.txtTime.visibility = View.GONE
                        binding.icSender.txtTime1.visibility = View.VISIBLE
                        binding.icSender.voicePlayerView.visibility = View.GONE

                        Glide.with(context).load(model.message).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.icSender.media)
                        binding.icSender.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //video
                    else if (model.message_type.equals(Constants.VIDEO)){
                        binding.icSender.mediaLayout.visibility = View.VISIBLE
                        binding.icSender.media.visibility = View.VISIBLE
                        binding.icSender.play.visibility = View.VISIBLE
                        binding.icSender.txtTime1.visibility = View.VISIBLE
                        binding.icSender.txtMsg.visibility = View.GONE
                        binding.icSender.doc.visibility = View.GONE
                        binding.icSender.voicePlayerView.visibility = View.GONE
                        binding.icSender.txtTime.visibility = View.GONE

                        Glide.with(context).asBitmap().load(model.message).diskCacheStrategy(DiskCacheStrategy.ALL)
                            .thumbnail(0.1f).into(binding.icSender.media)
                        binding.icSender.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //audio
                    else if (model.message_type.equals(Constants.AUDIO)){
                        binding.icSender.mediaLayout.visibility = View.VISIBLE
                        binding.icSender.voicePlayerView.visibility = View.VISIBLE
                        binding.icSender.txtTime.visibility = View.GONE
                        binding.icSender.media.visibility = View.GONE
                        binding.icSender.doc.visibility = View.GONE
                        binding.icSender.play.visibility = View.GONE
                        binding.icSender.txtMsg.visibility = View.GONE
                        binding.icSender.txtTime1.visibility = View.VISIBLE

                        binding.icSender.voicePlayerView.setAudio(model.message)
                        binding.icSender.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //docs
                    else if (model.message_type.equals(Constants.DOCS) || model.message_type.equals(Constants.PDF)){
                        binding.icSender.mediaLayout.visibility = View.VISIBLE
                        binding.icSender.media.visibility = View.VISIBLE
                        binding.icSender.play.visibility = View.GONE
                        binding.icSender.txtTime1.visibility = View.VISIBLE
                        binding.icSender.voicePlayerView.visibility = View.GONE
                        binding.icSender.txtMsg.visibility = View.GONE
                        binding.icSender.doc.visibility = View.VISIBLE
                        binding.icSender.txtTime.visibility = View.GONE

                        binding.icSender.txtTime1.text =
                            UtilsDefault.todayDate(UtilsDefault.localTimeConvert(model.date))
                    }

                    //location
                    else if (model.message_type.equals(Constants.LOCATION)){

                    }

                    else{

                    }
                }
            }

            //click event
            itemView.setOnClickListener {
                onItemClick!!.invoke(model)
            }


        }

    }

    internal fun setChat(chat: List<ChatDetailsRes>) {
        this.list = chat
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemChatNewBinding.bind(itemView)
    }

}