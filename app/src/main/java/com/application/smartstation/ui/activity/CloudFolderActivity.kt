package com.application.smartstation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCloudFolderBinding
import com.application.smartstation.databinding.ActivityCloudViewBinding
import com.application.smartstation.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudFolderActivity : BaseActivity() {

    val binding: ActivityCloudFolderBinding by viewBinding()
    var name = ""
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_folder)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null){
            name = intent.getStringExtra("name")!!
            id = intent.getStringExtra("id")!!
            val type = intent.getStringExtra("type")!!

            if (type.equals("send")){
                binding.llFab.visibility = View.VISIBLE
            }else{
                binding.llFab.visibility = View.GONE
            }

            binding.ilHeader.txtHeader.text = name
        }
    }

    private fun setOnClickListener() {

    }
}