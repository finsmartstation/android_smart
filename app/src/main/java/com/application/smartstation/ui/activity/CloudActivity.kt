package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCloudBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.CloudAdapter
import com.application.smartstation.ui.model.CloudListRes
import com.application.smartstation.ui.model.ContactListRes
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudActivity : BaseActivity() {

    val binding: ActivityCloudBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list: ArrayList<CloudListRes> = ArrayList()
    var cloudAdapter: CloudAdapter? = null
    var contactList: ArrayList<ContactListRes> = ArrayList()

    var showBtn: Animation? = null
    var hideBtn: Animation? = null
    var showLay: Animation? = null
    var hideLay: Animation? = null
    var name = ""

    companion object {
        val RESULT_CODE = 124
    }

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

        showBtn = AnimationUtils.loadAnimation(this, R.anim.show_btn)
        hideBtn = AnimationUtils.loadAnimation(this, R.anim.hide_btn)
        showLay = AnimationUtils.loadAnimation(this, R.anim.show_layout)
        hideLay = AnimationUtils.loadAnimation(this, R.anim.hide_layout)

//        phnPermission {
//            getContactList()
//        }

        cloudAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, CloudViewActivity::class.java).putExtra("name", model.phone)
                .putExtra("id", model.id))
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
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.cloud_numbers
                            if (list.isNotEmpty()) {
                                binding.rvCloud.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
//                                for (a in contactList) {
//                                    for (b in 0 until list.size) {
//                                        if (PhoneNumberUtils.compare(a.Phn, list[b].phone)) {
////                                            list.removeAt(b)
//                                            list.set(b,
//                                                CloudListRes(a.name,
//                                                    list[b].profile_pic,
//                                                    list[b].id,
//                                                    list[b].folder_name,
//                                                    list[b].user_id))
//                                        }
//                                    }
//                                }
                                setData(list)
                            } else {
                                binding.rvCloud.visibility = View.GONE
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

    private fun setData(list: ArrayList<CloudListRes>) {
        cloudAdapter!!.setCloud(list.reversed())
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

        binding.fabAdd.setOnClickListener {
            if (binding.llNewUser.visibility == View.VISIBLE) {
                binding.llNewUser.visibility = View.GONE
                binding.llNewUser.startAnimation(hideLay)
                binding.fabAdd.startAnimation(hideBtn)
            } else {
                binding.llNewUser.visibility = View.VISIBLE
                binding.llNewUser.startAnimation(showLay)
                binding.fabAdd.startAnimation(showBtn)
            }
        }

        binding.fabContact.setOnClickListener {
            binding.llNewUser.visibility = View.GONE
            binding.llNewUser.startAnimation(hideLay)
            binding.fabAdd.startAnimation(hideBtn)
            startActivityForResult(Intent(this, UserDetailsActivity::class.java), RESULT_CODE)
        }

        binding.llNewUser.setOnClickListener {
            binding.llNewUser.visibility = View.GONE
            binding.llNewUser.startAnimation(hideLay)
            binding.fabAdd.startAnimation(hideBtn)
            startActivityForResult(Intent(this, UserDetailsActivity::class.java), RESULT_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            124 -> {
                if (data != null) {
                    val id = data.getStringExtra("id")
                    name = data.getStringExtra("name")!!
                    createCloudFolder(id)
                }
            }
        }
    }

    private fun createCloudFolder(id: String?) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.receiver_id = id

        apiViewModel.createCloudFolder(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            getCloud()
                        } else {
                            if(it.data.message.equals("Folder already exist")){
                                startActivity(Intent(this, CloudViewActivity::class.java).putExtra("name", name)
                                    .putExtra("id", it.data.cloud_id))
                            }else {
                                toast(it.data.message)
                            }
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

    @SuppressLint("Range")
    private fun getContactList() {
        try {
            val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
            while (phones!!.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                contactList!!.add(ContactListRes(name,phoneNumber))
                Log.d("name>>", name + "  " + phoneNumber)
            }
            phones.close()
        } catch (e: Exception) {
            Log.d("TAG", "getContactList: " + e)
        }
    }
}