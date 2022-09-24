package com.application.smartstation.ui.activity

import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCloudViewBinding
import com.application.smartstation.ui.fragment.CloudRecFragment
import com.application.smartstation.ui.fragment.CloudSendFragment
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudViewActivity : BaseActivity() {

    val binding: ActivityCloudViewBinding by viewBinding()
    var name = ""
    var id = ""
    var fragmentHelper: FragmentHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_view)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null){
            name = intent.getStringExtra("name")!!
            id = intent.getStringExtra("id")!!
            binding.ilHeader.txtHeader.text = name
        }

        setUpFragments()

    }

    private fun setUpFragments() {
        fragmentHelper = FragmentHelper(supportFragmentManager)
        fragmentHelper?.setUpFrame(CloudRecFragment(id), binding.cloudFrameContainer)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.rlRec.setOnClickListener {
            binding.rlRec.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlSent.background = null
            fragmentHelper?.push(CloudRecFragment(id))
        }

        binding.rlSent.setOnClickListener {
            binding.rlRec.background = null
            binding.rlSent.background = getDrawable(R.color.color_tab_select_bg)
            fragmentHelper?.push(CloudSendFragment(id))
        }
    }
}