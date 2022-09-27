package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCreateGroupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.AddGroupAdapter
import com.application.smartstation.ui.adapter.ContactGroupAdapter
import com.application.smartstation.ui.model.ContactListRes
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateGroupActivity : BaseActivity() {

    val binding: ActivityCreateGroupBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list: ArrayList<DataUserList> = ArrayList()

    companion object {
        var listAdd: ArrayList<DataUserList> = ArrayList()
    }

    var contactAdapter: ContactGroupAdapter? = null
    var addGrpAdapter: AddGroupAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    var user = ""
    var contactList: ArrayList<ContactListRes> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.add_participants)

        binding.rvUser.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        contactAdapter = ContactGroupAdapter(this)
        binding.rvUser.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model, pos ->
            addGrp(model, pos)
        }

        phnPermission {
            getContactList()
        }

        user = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!

        layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvSelectUser.layoutManager = layoutManager
        addGrpAdapter = AddGroupAdapter(this)
        binding.rvSelectUser.adapter = addGrpAdapter

        addGrpAdapter!!.onItemClick = { model, pos ->
            listAdd.removeAt(pos)
            if (!listAdd.isNullOrEmpty()) {
                binding.llSelectUser.visibility = View.VISIBLE
                addGrpAdapter!!.setChat(listAdd, true)
                layoutManager!!.scrollToPosition(addGrpAdapter!!.itemCount - 1)
            } else {
                binding.llSelectUser.visibility = View.GONE
            }

            for (i in 0 until list.size) {
                if (model.user_id.equals(list[i].user_id)) {
                    list.set(i,
                        DataUserList(model.user_id,
                            model.name,
                            model.profile_pic,
                            model.phone,
                            model.country,
                            model.about,
                            false))
                    contactAdapter!!.setChats(i)
                    break
                }
            }

            user = user.replace("," + model.user_id, "")
        }

        getUserDetails()
    }

    private fun addGrp(model: DataUserList, pos: Int) {
        if (!model.statusSelected) {
            list.set(pos,
                DataUserList(model.user_id,
                    model.name,
                    model.profile_pic,
                    model.phone,
                    model.country,
                    model.about,
                    true))
            listAdd.add(DataUserList(model.user_id,
                model.name,
                model.profile_pic,
                model.phone,
                model.country,
                model.about,
                true))
            user = user + "," + model.user_id
            Log.d("TAG", "addGrp: " + user)
        } else {
            for (i in listAdd) {
                if (model.user_id.equals(i.user_id)) {
                    listAdd.remove(i)
                    break
                }
            }
            list.set(pos,
                DataUserList(model.user_id,
                    model.name,
                    model.profile_pic,
                    model.about,
                    model.phone,
                    model.country,
                    false))
            user = user.replace("," + model.user_id, "")
            Log.d("TAG", "addGrp: " + user)
        }
        contactAdapter!!.setChats(pos)

        if (!listAdd.isNullOrEmpty()) {
            binding.llSelectUser.visibility = View.VISIBLE
            addGrpAdapter!!.setChat(listAdd, true)
            layoutManager!!.scrollToPosition(addGrpAdapter!!.itemCount - 1)
        } else {
            binding.llSelectUser.visibility = View.GONE
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.fbSelectGrp.setOnClickListener {
            if (!user.isNullOrEmpty() && !user.equals(UtilsDefault.getSharedPreferenceString(
                    Constants.USER_ID))
            ) {
                user = user.replace(UtilsDefault.getSharedPreferenceString(Constants.USER_ID) + ",",
                    "")
                createGrp(user, listAdd)
            } else {
                toast("Please select user")
            }
        }
    }

    private fun createGrp(user: String, listAdd: ArrayList<DataUserList>) {
        val intent = Intent(this, AddGroupActivity::class.java)
        intent.putExtra("members", user)
        intent.putExtra("type", 1)
        startActivity(intent)
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
                            list = it.data.data
                            if (list.isNotEmpty()) {
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
        for (a in contactList) {
            for (b in 0 until list.size) {
                if (PhoneNumberUtils.compare(a.Phn, list[b].phone)) {
                    list.add(DataUserList(list[b].user_id,
                        a.name,
                        list[b].profile_pic,
                        list[b].phone,
                        list[b].country,
                        list[b].about))
                }
            }
        }
        contactAdapter!!.setChat(list)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        listAdd.clear()
    }

    @SuppressLint("Range")
    private fun getContactList() {
        try {
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
        } catch (e: Exception) {
            Log.d("TAG", "getContactList: " + e)
        }

    }
}