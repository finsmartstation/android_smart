package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityUserDetailsBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.ContactAdapter
import com.application.smartstation.ui.model.ContactListRes
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
    var list: ArrayList<DataUserList> = ArrayList()
    var contactAdapter: ContactAdapter? = null
    var contactList: ArrayList<ContactListRes> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.rvContact.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        contactAdapter = ContactAdapter(this)
        binding.rvContact.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model ->
            val intent = Intent()
            intent.putExtra("id", model.user_id)
            setResult(RESULT_OK, intent)
            finish()
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

        phnPermission {
            getContactList()
        }


    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<DataUserList> = ArrayList()

            for (items in list) {
                val chat = items.name.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            contactAdapter?.setChat(templist)
        } else {
            contactAdapter?.setChat(list)
        }
    }

    private fun getUserDetails() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getUserlist(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            var list1 = it.data.data
                            if (list1.isNotEmpty()) {
                                list.clear()
                                for (a in contactList) {
                                    for (b in list1) {
                                        if (PhoneNumberUtils.compare(a.Phn, b.phone)) {
                                            list.add(DataUserList(b.user_id,
                                                a.name,
                                                b.profile_pic,
                                                b.phone,
                                                b.country,
                                                b.about))
                                        }
                                    }
                                }
                                setData(list)
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

    private fun setData(list: ArrayList<DataUserList>) {
        contactAdapter!!.setChat(list)
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

    @SuppressLint("Range")
    private fun getContactList() {
        val cr = contentResolver
        val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null)
        if ((if (cur != null) cur.count else 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name: String = cur.getString(cur.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME))
                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null)
                    while (pCur!!.moveToNext()) {
                        val phoneNo: String = pCur.getString(pCur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER))
                        contactList.add(ContactListRes(name, phoneNo))
                    }
                    pCur.close()
                }
            }
        }
        if (cur != null) {
            cur.close()
        }
    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }
}