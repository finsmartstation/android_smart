package com.application.smartstation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatInfoBinding
import com.application.smartstation.databinding.ActivityPrivateChatInfoBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    }

    private fun setOnClickListener() {
        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }
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
                            binding.txtDesTime.text = UtilsDefault.localTimeConvert(UtilsDefault.dateConvert(it.data.data.receiver_data.about_updated_datetime))
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
}