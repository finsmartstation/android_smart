package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCloudBinding
import com.application.smartstation.databinding.ActivityEditProfileBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.CloudAdapter
import com.application.smartstation.ui.adapter.SentboxAdapter
import com.application.smartstation.ui.model.CloudListRes
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.SendMailListRes
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudActivity : BaseActivity() {

    val binding: ActivityCloudBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<CloudListRes> = ArrayList()
    var cloudAdapter: CloudAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.rvCloud.layoutManager = GridLayoutManager(this, 3)
        cloudAdapter = CloudAdapter(this)
        binding.rvCloud.adapter = cloudAdapter

        cloudAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, CloudViewActivity::class.java).putExtra("name",model.phone))
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

        getCloud()
    }

    private fun getCloud() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getCloud(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.cloud_numbers
                            if(list.isNotEmpty()) {
                                binding.rvCloud.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            }else{
                                binding.rvCloud.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setData(list: ArrayList<CloudListRes>) {
        cloudAdapter!!.setCloud(list)
    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<CloudListRes> = ArrayList()

            for (items in list) {
                val chat = items.phone.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            cloudAdapter?.setCloud(templist)
        } else {
            cloudAdapter?.setCloud(list)
        }
    }

    private fun setOnClickListener() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgSearch.setOnClickListener {
            binding.llSearch.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.imgCancel.visibility = View.VISIBLE
        }

        binding.imgCancel.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE
        }

    }
}