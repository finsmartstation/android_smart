package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatBinding
import com.application.smartstation.databinding.ActivityChatInfoBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.ChatHistoryAdapter
import com.application.smartstation.ui.adapter.UserListGrpAdapter
import com.application.smartstation.ui.model.GrpUserListRes
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.UserListGrp
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_chat_info.view.*

@AndroidEntryPoint
class ChatInfoActivity : BaseActivity() {

    val binding: ActivityChatInfoBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var name = ""
    var profilePic = ""
    var chatType = ""
    var room = ""
    companion object {
        var list = ArrayList<UserListGrp>()
    }
    var userListGrpAdapter: UserListGrpAdapter? = null
    var more = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        if (intent != null){
            name = intent.getStringExtra(Constants.NAME)!!
            profilePic = intent.getStringExtra(Constants.PROFILE_PIC)!!

            if (intent.getStringExtra(Constants.CHAT_TYPE) != null){
                chatType = intent.getStringExtra(Constants.CHAT_TYPE)!!
            }else{
                chatType = "private"
            }

            if (intent.getStringExtra(Constants.ROOM) != null){
                room = intent.getStringExtra(Constants.ROOM)!!
            }

            binding.txtName.text = name
            Glide.with(this).load(profilePic).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA).into(binding.imgProfile)
        }

        binding.rvChatInfo.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        userListGrpAdapter = UserListGrpAdapter(this)
        binding.rvChatInfo.adapter = userListGrpAdapter
        binding.rvChatInfo.setHasFixedSize(false)


        binding.appBarLayout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    //when collapsingToolbar at that time display actionbar title
                    binding.toolbar.txtHeader.text = name
                    isShow = true
                } else if (isShow) {
                    if (chatType.equals("private")) {
                        binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
                    }else{
                        binding.toolbar.txtHeader.text = resources.getString(R.string.group_info)
                    }
                    isShow = false
                }
            }
        })
    }

    private fun getUserListGrp() {

        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.group_id = room

        apiViewModel.getGrpUserList(inputParams).observe(this, Observer {
            it.let {
                when(it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            if (it.data.data.size != 0){ 
                                setData(it.data.data)
                                binding.txtParticipants.text =resources.getString(R.string.group)+" " +it.data.data.size+" "+resources.getString(R.string.participants)
                                binding.txtParticipants1.text = it.data.data.size.toString()+" "+resources.getString(R.string.participants)
                                for (i in 0 until it.data.data.size){
                                    if (UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!.equals(it.data.data[i].user_id)){
                                        if (it.data.data[i].type.equals("admin")){
                                            binding.llAddContact.visibility = View.VISIBLE
                                        }else{
                                            binding.llAddContact.visibility = View.GONE
                                        }
                                    }
                                }
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

    private fun setData(data: ArrayList<UserListGrp>) {
        list = data
        if (list.size>10){
            binding.txtView.visibility = View.VISIBLE
            userListGrpAdapter!!.setUser(list.subList(0,10))
        }else{
            binding.txtView.visibility = View.GONE
            userListGrpAdapter!!.setUser(list)
        }
    }

    private fun setOnClickListener() {
        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }

        binding.txtView.setOnClickListener {
//            if (more){
//                binding.txtView.text = resources.getString(R.string.show_more)
//                more = false
//                userListGrpAdapter!!.setUser(list)
//            }else{
//                binding.txtView.text = resources.getString(R.string.view_all)
//                more = true
//                if (list.size>10){
//                    userListGrpAdapter!!.setUser(list.subList(0,9))
//                }else{
//                    userListGrpAdapter!!.setUser(list)
//                }
//            }
        }

        binding.llAddContact.setOnClickListener{
            startActivity(Intent(this,ExtraAddGroupActivity::class.java).putExtra("room",room))
        }
    }

    override fun onResume() {
        super.onResume()
        if (chatType.equals("private")) {
            binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
            binding.llGrpPart.visibility = View.GONE
        }else{
            binding.toolbar.txtHeader.text = resources.getString(R.string.group_info)
            binding.llGrpPart.visibility = View.VISIBLE
            getUserListGrp()
        }
    }
}