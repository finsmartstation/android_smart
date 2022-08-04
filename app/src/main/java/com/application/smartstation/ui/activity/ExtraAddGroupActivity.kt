package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityExtraAddGroupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.AddGroupAdapter
import com.application.smartstation.ui.adapter.ContactGroupAdapter
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtraAddGroupActivity : BaseActivity() {

    val binding: ActivityExtraAddGroupBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<DataUserList> = ArrayList()
    companion object{
        var listAdd:ArrayList<DataUserList> = ArrayList()
    }
    var contactAdapter: ContactGroupAdapter? = null
    var addGrpAdapter: AddGroupAdapter? = null
    var layoutManager: LinearLayoutManager? = null
    var user = ""
    var room = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extra_add_group)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.add_participants)

        binding.rvUser.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        contactAdapter = ContactGroupAdapter(this)
        binding.rvUser.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model,pos ->
            addGrp(model,pos)
        }

        if (intent != null){
            room = intent.getStringExtra("room")!!
        }

        user = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!

        layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,false)
        binding.rvSelectUser.layoutManager = layoutManager
        addGrpAdapter = AddGroupAdapter(this)
        binding.rvSelectUser.adapter = addGrpAdapter

        addGrpAdapter!!.onItemClick = { model, pos ->
            listAdd.removeAt(pos)
            if (!listAdd.isNullOrEmpty()){
                binding.llSelectUser.visibility = View.VISIBLE
                addGrpAdapter!!.setChat(listAdd,true)
                layoutManager!!.scrollToPosition(addGrpAdapter!!.getItemCount() - 1)
            }else{
                binding.llSelectUser.visibility = View.GONE
            }

            for (i in 0 until list.size){
                if (model.user_id.equals(list[i].user_id)){
                    list.set(i,
                        DataUserList(model.user_id, model.name, model.profile_pic, model.about, false))
                    contactAdapter!!.setChats(i)
                    break
                }
            }

            user = user.replace(","+model.user_id,"")
        }

        getUserDetails()
    }

    private fun addGrp(model: DataUserList, pos: Int) {
        if (!model.statusSelected) {
            list.set(pos,
                DataUserList(model.user_id, model.name, model.profile_pic, model.about, true))
            listAdd.add(DataUserList(model.user_id, model.name, model.profile_pic, model.about,true))
            user = user+","+model.user_id
            Log.d("TAG", "addGrp: "+user)
        }else{
            for (i in CreateGroupActivity.listAdd){
                if (model.user_id.equals(i.user_id)){
                    listAdd.remove(i)
                    break
                }
            }
            list.set(pos,
                DataUserList(model.user_id, model.name, model.profile_pic, model.about, false))
            user = user.replace(","+model.user_id,"")
            Log.d("TAG", "addGrp: "+user)
        }
        contactAdapter!!.setChats(pos)

        if (!listAdd.isNullOrEmpty()){
            binding.llSelectUser.visibility = View.VISIBLE
            addGrpAdapter!!.setChat(listAdd,true)
            layoutManager!!.scrollToPosition(addGrpAdapter!!.getItemCount() - 1)
        }else{
            binding.llSelectUser.visibility = View.GONE
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.fbSelectGrp.setOnClickListener {
            if (!user.isNullOrEmpty() && !user.equals(UtilsDefault.getSharedPreferenceString(
                    Constants.USER_ID))) {
                user = user.replace(UtilsDefault.getSharedPreferenceString(Constants.USER_ID)+",","")
                createGrp(user)
            }else{
                toast("Please select user")
            }
        }
    }

    private fun createGrp(user: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.group_id = room
        inputParams.members = user

        apiViewModel.addGrpUser(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            finish()
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
                            if(!list.isNullOrEmpty()) {
                                for (i in 0 until ChatInfoActivity.list.size){
                                    for (j in 0 until list.size){
                                        if (ChatInfoActivity.list[i].user_id.equals(list[j].user_id)){
                                            list.set(j,
                                                DataUserList(list[j].user_id, list[j].name, list[j].profile_pic, list[j].about, false,true))
                                        }
                                    }
                                }
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

    override fun onBackPressed() {
        super.onBackPressed()
        listAdd.clear()
    }
}