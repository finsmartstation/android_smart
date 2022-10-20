package com.application.smartstation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMediaBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.fragment.CloudRecFragment
import com.application.smartstation.ui.fragment.CloudSendFragment
import com.application.smartstation.ui.model.GetMedia
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.MediaRes
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaActivity : BaseActivity() {

    val binding: ActivityMediaBinding by viewBinding()
    var id = ""
    var type = ""
    val apiViewModel: ApiViewModel by viewModels()
    var mediaList:ArrayList<MediaRes> = ArrayList()
    var linkList:ArrayList<MediaRes> = ArrayList()
    var docsList:ArrayList<MediaRes> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.rlMedia.setOnClickListener {
            binding.rlMedia.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlLink.background = null
            binding.rlDocs.background = null
        }

        binding.rlLink.setOnClickListener {
            binding.rlMedia.background = null
            binding.rlLink.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlDocs.background = null
        }

        binding.rlDocs.setOnClickListener {
            binding.rlMedia.background = null
            binding.rlLink.background = null
            binding.rlDocs.background = getDrawable(R.color.color_tab_select_bg)
        }
    }

    private fun initView() {
        if (intent != null) {
            id = intent.getStringExtra("id")!!
            type = intent.getStringExtra("type")!!
            binding.ilHeader.txtHeader.text = resources.getString(R.string.media_link)
        }

        if (!type.isNullOrEmpty()){
            if (type.equals("private")){
                getPrivateMedia()
            }else{
                getGrpMedia()
            }
        }
    }

    private fun getGrpMedia() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.group_id = id

        apiViewModel.getGrpMedia(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {

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

    private fun getPrivateMedia() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = id

        apiViewModel.getPrivateMedia(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (!it.data.data.medias.isNullOrEmpty()){
                                mediaList = it.data.data.medias
                            }

                            if (!it.data.data.links.isNullOrEmpty()){
                                linkList = it.data.data.links
                            }

                            if (!it.data.data.docs.isNullOrEmpty()){
                                docsList = it.data.data.docs
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
}