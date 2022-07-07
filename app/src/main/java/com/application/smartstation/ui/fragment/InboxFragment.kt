package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentInboxBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.NewMailActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.adapter.InboxAdapter
import com.application.smartstation.ui.model.ChatResponse
import com.application.smartstation.ui.model.DataMailList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxFragment : BaseFragment(R.layout.fragment_inbox) {

    private val binding by viewBinding(FragmentInboxBinding::bind)

    var list:ArrayList<DataMailList> = ArrayList()
    var inboxAdapter: InboxAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {

    }

    private fun initView() {

        getMailList()


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
        inboxAdapter!!.setMail(list)

        inboxAdapter!!.onItemClick = { model ->

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

}