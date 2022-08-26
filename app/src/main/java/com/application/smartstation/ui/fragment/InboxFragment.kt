package com.application.smartstation.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentInboxBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.activity.NewMailActivity
import com.application.smartstation.ui.activity.ViewMailActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.adapter.InboxAdapter
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.application.smartstation.viewmodel.InboxEvent
import com.application.smartstation.viewmodel.RecentChatEvent
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

@AndroidEntryPoint
class InboxFragment : BaseFragment(R.layout.fragment_inbox) {

    private val binding by viewBinding(FragmentInboxBinding::bind)

    var list:ArrayList<DataMailList> = ArrayList()
    var inboxAdapter: InboxAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()
    var emitters: SocketService.Emitters? = null

    var mCallback: OnUnreadMailCountListener? = null

    interface OnUnreadMailCountListener {
        fun onUnreadMail(count: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        emitters = SocketService.Emitters(context)
        try {
            mCallback = activity as OnUnreadMailCountListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {

    }

    private fun initView() {

        emitInbox()

    }

    private fun emitInbox() {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("user_id", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("accessToken", UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                emitters!!.mailList(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getMailList() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getMaillist(inputParams).observe(requireActivity(), Observer {
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

    private fun setData(list: ArrayList<DataMailList>) {
        binding.rvInbox.layoutManager = LinearLayoutManager(requireActivity(),
            LinearLayoutManager.VERTICAL,false)
        inboxAdapter = InboxAdapter(requireActivity())
        binding.rvInbox.adapter = inboxAdapter
        inboxAdapter!!.setMail(list.reversed())

        if (list.isNullOrEmpty()){
            unreadMail("0")
        }else{
            var count = 0
            for (i in 0 until list.size){
                if (list[i].mail_read_status.equals(0)){
                    unreadMail(count.toString())
                }else{
                    count = +1
                }
            }
            unreadMail(count.toString())
        }

        inboxAdapter!!.onItemClick = { model ->
            val intent = Intent(requireActivity(), ViewMailActivity::class.java)
            intent.putExtra("boxType",1)
            intent.putExtra("id",model.id)
            startActivity(intent)
        }
    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<DataMailList> = ArrayList()

            for (items in list) {
                val coinsy = items.datetime.toLowerCase()
                if (coinsy.contains(searchtext)) {
                    templist.add(items)
                }

            }

//            chatAdapter?.setChat(templist)
        } else {
//            chatAdapter?.setChat(list)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInboxEvent(event: InboxEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setInboxEvent(jsonObject)
        Log.d("TAG", "ONCHATEVENT: "+jsonObject)
    }

    fun setInboxEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val mailSocketModel: GetMailListResponse = gson.fromJson(jsonObject.toString(),
            GetMailListResponse::class.java)
        if (mailSocketModel.status){
            if (mailSocketModel.data.isNotEmpty()){
                setData(mailSocketModel.data)
            }
        }else{
            toast(mailSocketModel.message)
        }
    }

    override fun onResume() {
        super.onResume()
        getMailList()
    }

    fun unreadMail(count:String){
        mCallback!!.onUnreadMail(count)
    }

}