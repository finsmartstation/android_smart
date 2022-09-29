package com.application.smartstation.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentLetterInboxBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.ui.activity.ViewLetterActivity
import com.application.smartstation.ui.adapter.ReceivedLetterAdapter
import com.application.smartstation.ui.model.DataLetter
import com.application.smartstation.ui.model.DataMailList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LetterInboxFragment : BaseFragment(R.layout.fragment_letter_inbox) {

    private val binding by viewBinding(FragmentLetterInboxBinding::bind)

    var list: ArrayList<DataLetter> = ArrayList()
    var receivedLetterAdapter: ReceivedLetterAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()

    var mCallback: OnUnreadLetterCountListener? = null

    interface OnUnreadLetterCountListener {
        fun onUnreadLetter(count: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mCallback = activity as OnUnreadLetterCountListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.rvReceivedLetter.layoutManager = LinearLayoutManager(requireActivity(),
            LinearLayoutManager.VERTICAL, false)
        receivedLetterAdapter = ReceivedLetterAdapter(requireActivity())
        binding.rvReceivedLetter.adapter = receivedLetterAdapter

        receivedLetterAdapter!!.onItemClick = { model ->
            val intent = Intent(requireActivity(), ViewLetterActivity::class.java)
            intent.putExtra("boxType", 1)
            intent.putExtra("id", model.id)
            requireActivity().startActivity(intent)
        }

        (activity as MainActivity?)!!.setSendData(object : MainActivity.SearchInterface {
            override fun searchData(searchTxt: String, type: String) {
                if (type.equals("letter")) {
                    filterList(searchTxt)
                }
            }

        })
    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<DataLetter> = ArrayList()

            for (items in list) {
                val coinsy = items.from.toLowerCase()+items.subject.toLowerCase()
                if (coinsy.contains(searchtext)) {
                    templist.add(items)
                }

            }

            receivedLetterAdapter!!.setReceivedLetter(templist)
        } else {
            receivedLetterAdapter!!.setReceivedLetter(list.reversed())
        }
    }

    private fun setOnClickListener() {

    }

    override fun onResume() {
        super.onResume()
        getLetter()
    }

    private fun getLetter() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getLetter(inputParams).observe(requireActivity(), Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.data.letter_list
                            unreadLetter(it.data.data.unread.toString())
                            if (list.isNotEmpty()) {
                                binding.rvReceivedLetter.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            } else {
                                binding.rvReceivedLetter.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
                            }
                        } else {
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

    private fun setData(list: ArrayList<DataLetter>) {
        receivedLetterAdapter!!.setReceivedLetter(list.reversed())
    }

    fun unreadLetter(count: String) {
        mCallback!!.onUnreadLetter(count)
    }
}