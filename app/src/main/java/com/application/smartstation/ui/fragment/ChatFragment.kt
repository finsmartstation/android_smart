package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentChatBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.ChatActivity
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class ChatFragment : BaseFragment(R.layout.fragment_chat){

    private val binding by viewBinding(FragmentChatBinding::bind)

    var list:ArrayList<DataChatList> = ArrayList()
    var chatAdapter: ChatAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()
    val mainHandler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        startTimer()

        binding.rvChat.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        chatAdapter = ChatAdapter(requireActivity())
        binding.rvChat.adapter = chatAdapter

        chatAdapter!!.onItemClick = { model ->
            startActivity(Intent(requireActivity(), ChatActivity::class.java).putExtra(Constants.REC_ID,model.userid).putExtra(Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile))
        }

        getChatList()

//        binding.edtSearch.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val txt = s.toString()
//                filterList(txt)
//
//            }
//        })

        (activity as MainActivity?)!!.setSendData(object : MainActivity.SearchInterface {
            override fun searchData(searchTxt: String, type: String) {
                if (type.equals("chat")) {
                    filterList(searchTxt)
                }
            }

        })

    }

    private fun getChatList() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getChatlist(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.data
                            if(list.isNotEmpty()) {
                                setData(list)
                            }
                        }else{
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        dismissProgress()
                        toast(it.message!!)
                    }
                }
            }
        })
    }

    private fun setData(list: ArrayList<DataChatList>) {
        chatAdapter!!.setChat(list)
    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<DataChatList> = ArrayList()

            for (items in list) {
                val chat = items.name.toLowerCase()+items.message.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            chatAdapter?.setChat(templist)
        } else {
            chatAdapter?.setChat(list)
        }
    }

    private fun setOnClickListener() {

    }

    private val updateChat = object : Runnable {
        override fun run() {
            Log.d("TAG", "runChat: "+"aaaaaaaa")
            getChatListRefresh()
            mainHandler.postDelayed(this, 3000)
        }
    }

    private fun getChatListRefresh() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        try {
            apiViewModel.getChatlist(inputParams).observe(requireActivity(), Observer {
                it.let {
                    when(it.status){
                        Status.LOADING -> {
                        }
                        Status.SUCCESS -> {
                            if (it.data!!.status){
                                list = it.data.data
                                if(list.isNotEmpty()) {
                                    setData(list)
                                }
                            }else{
                                toast(it.data.message)
                            }
                        }
                        Status.ERROR -> {
                            toast(it.message!!)
                        }
                    }
                }
            })
        }catch (e:Exception){
            Log.d("TAG", "getChatListRefresh: "+e)
        }

    }

    fun startTimer(){
        mainHandler.post(updateChat)
    }

    fun stopTimer(){
        mainHandler.removeCallbacks(updateChat)
    }

}