package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentContactChatBinding
import com.application.smartstation.ui.activity.ChatActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.util.Constants
import com.application.smartstation.util.viewBinding

class ContactChatFragment : BaseFragment(R.layout.fragment_contact_chat) {

    private val binding by viewBinding(FragmentContactChatBinding::bind)

    var list: ArrayList<ChatResponse> = ArrayList()
    var chatAdapter: ChatAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
//        list.add(ChatResponse("Anna Bella","Great! Thank you So much","11.54 PM",R.drawable.pht,1,10))
//        list.add(ChatResponse("Marc Stegen","hey wassup.. whats going on","10.30 PM",R.drawable.pht1,1,5))
//        list.add(ChatResponse("The Kop Fans","Stevie: Hai, h r u","8.30 PM",R.drawable.pht2,1,100))
//        list.add(ChatResponse("Philipe Louis","You: I shall do that!","10.15 AM",R.drawable.pht3,0,0))
//        list.add(ChatResponse("Zhen Zou","Great! Thank you So much","4.30 PM",R.drawable.pht4,0,0))
//        list.add(ChatResponse("Ji Sung","hey wassup.. whats going on","7.30 PM",R.drawable.pht5,0,0))
//        list.add(ChatResponse("Aguilera","Stevie: Hai, h r u","9.30 PM",R.drawable.pht5,0,0))


        binding.rvContact.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        chatAdapter = ChatAdapter(requireActivity())
        binding.rvContact.adapter = chatAdapter
//        chatAdapter!!.setChat(list)

        chatAdapter!!.onItemClick = { model ->
            startActivity(Intent(requireActivity(),
                ChatActivity::class.java).putExtra(Constants.NAME, model.name)
                .putExtra(Constants.PROFILE, model.profile))
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txt = s.toString()
                filterList(txt)

            }
        })

        Log.d("TAG", "initView: " + "cccc")

    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<ChatResponse> = ArrayList()

            for (items in list) {
                val coinsy = ""
//                    items.name.toLowerCase()
                if (coinsy.contains(searchtext)) {
                    templist.add(items)
                }

            }

//            chatAdapter?.setChat(templist)
        } else {
//            chatAdapter?.setChat(list)
        }
    }

    private fun setOnClickListener() {

    }
}