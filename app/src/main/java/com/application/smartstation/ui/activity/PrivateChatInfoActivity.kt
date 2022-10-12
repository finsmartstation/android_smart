package com.application.smartstation.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatInfoBinding
import com.application.smartstation.databinding.ActivityPrivateChatInfoBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.PrivateChatInfoGrpAdapter
import com.application.smartstation.ui.adapter.UserListGrpAdapter
import com.application.smartstation.ui.model.CommonGrpList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import io.realm.internal.Util
import kotlinx.android.synthetic.main.activity_chat_info.view.*
@AndroidEntryPoint
class PrivateChatInfoActivity : BaseActivity() {

    val binding: ActivityPrivateChatInfoBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var name = ""
    var profilePic = ""
    var chatType = ""
    var receiver_id = ""
    var privateChatInfoGrpAdapter: PrivateChatInfoGrpAdapter? = null
    var CAMERA_MIC_PERMISSION_REQUEST_CODE = 791

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat_info)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null) {
            if (intent.getStringExtra(Constants.REC_ID) != null) {
                receiver_id = intent.getStringExtra(Constants.REC_ID)!!
            }
        }

        binding.rvChatInfo.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        privateChatInfoGrpAdapter = PrivateChatInfoGrpAdapter(this)
        binding.rvChatInfo.adapter = privateChatInfoGrpAdapter

        privateChatInfoGrpAdapter!!.onItemClick = { model ->
            startActivity(Intent(this,
                ChatViewGrpActivity::class.java).putExtra(Constants.REC_ID, "")
                .putExtra(Constants.NAME, model.group_name).putExtra(Constants.PROFILE, model.group_profile_pic)
                .putExtra(Constants.CHAT_TYPE, "group")
                .putExtra(Constants.ROOM, model.group_id))
        }

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
                    binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
                    isShow = false
                }
            }
        })
    }

    private fun setOnClickListener() {
        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }

        binding.llAudio.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                calling("voice_call")
            }
        }

        binding.llVideo.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                calling("video_call")
            }
        }

    }

    private fun calling(type: String) {
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(Constants.REC_ID, receiver_id)
        intent.putExtra(Constants.REC_NAME, name)
        intent.putExtra(Constants.REC_PROFILE, profilePic)
        intent.putExtra(Constants.CALL_TYPE, type)
        intent.putExtra(Constants.STATUS, "Call_Send")
        intent.putExtra(Constants.ROOM_NAME, UtilsDefault.getRandomString(10))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        getPrivateInfo()
    }

    private fun getPrivateInfo() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiver_id

        apiViewModel.getPrivateInfo(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            name = it.data.data.receiver_data.name
                            profilePic = it.data.data.receiver_data.profile_pic
                            binding.txtName.text = it.data.data.receiver_data.name
                            binding.txtViewDes.text = it.data.data.receiver_data.about
                            binding.txtParticipants.text = it.data.data.receiver_data.phone
                            binding.txtMediaCount.text = it.data.data.media_count.toString()
                            binding.txtCreateGrp.text = resources.getString(R.string.create_grp)+" "+resources.getString(R.string.with)+" "+it.data.data.receiver_data.name
                            if (it.data.data.common_group_data.no_of_groups.equals(0)){
                                binding.txtParticipants1.text ="No "+resources.getString(R.string.grp_in_common)
                            }else{
                                binding.txtParticipants1.text = it.data.data.common_group_data.no_of_groups.toString()+" "+resources.getString(R.string.grp_in_common)
                            }
                            if (!it.data.data.common_group_data.data.isNullOrEmpty()){
                                binding.rvChatInfo.visibility = View.VISIBLE
                                setData(it.data.data.common_group_data.data)
                            }else{
                                binding.rvChatInfo.visibility = View.GONE
                            }
                            binding.txtDesTime.text = UtilsDefault.dateConvert(UtilsDefault.localTimeConvert(it.data.data.receiver_data.about_updated_datetime))
                            Glide.with(this).load(it.data.data.receiver_data.profile_pic).into(binding.imgProfile)
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

    private fun setData(data: ArrayList<CommonGrpList>) {
        privateChatInfoGrpAdapter!!.setChat(data)
    }

    fun checkPermissionForCameraAndMicrophone(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val resultCamera =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val resultMic =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            resultCamera == PackageManager.PERMISSION_GRANTED &&
                    resultMic == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermissionForCameraAndMicrophone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(
                    Manifest.permission.RECORD_AUDIO)
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE)
            }
        }
    }

}