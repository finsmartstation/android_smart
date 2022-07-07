package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMainBinding
import com.application.smartstation.databinding.ActivityUserDetailsBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.adapter.ContactAdapter
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailsActivity : BaseActivity() {

    val binding: ActivityUserDetailsBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<DataUserList> = ArrayList()
    var contactAdapter: ContactAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.rvContact.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        contactAdapter = ContactAdapter(this)
        binding.rvContact.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, ChatActivity::class.java).putExtra(Constants.REC_ID,model.user_id).putExtra(Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile_pic))
        }

        getUserDetails()
    }

    private fun getUserDetails() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getUserlist(inputParams).observe(this, Observer {
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

    private fun setData(list: ArrayList<DataUserList>) {
        contactAdapter!!.setChat(list)
    }

    private fun setOnClickListener() {
        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgCancel.setOnClickListener {
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE
            UtilsDefault.hideKeyboardForFocusedView(this)
        }
    }
}