package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCloudBinding
import com.application.smartstation.databinding.ActivityCloudViewBinding
import com.application.smartstation.ui.adapter.CloudAdapter
import com.application.smartstation.ui.adapter.CloudViewAdapter
import com.application.smartstation.ui.model.CloudListRes
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel

class CloudViewActivity : BaseActivity() {

    val binding: ActivityCloudViewBinding by viewBinding()
    var name = ""
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<CloudListRes> = ArrayList()
    var cloudViewAdapter: CloudViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_view)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null){
            name = intent.getStringExtra("name")!!
            binding.ilHeader.txtHeader.text = name
        }

        binding.rvCloudView.layoutManager = GridLayoutManager(this, 3)
        cloudViewAdapter = CloudViewAdapter(this)
        binding.rvCloudView.adapter = cloudViewAdapter

        list.add(CloudListRes("+91 8122335880",R.drawable.pht))
        list.add(CloudListRes("+91 9995330811",R.drawable.akhil))
        list.add(CloudListRes("+91 8122335808",R.drawable.pht1))
        list.add(CloudListRes("+91 8122335000",R.drawable.pht2))
        list.add(CloudListRes("+91 8122335801",R.drawable.pht3))
        list.add(CloudListRes("+91 8122332380",R.drawable.pht4))
        list.add(CloudListRes("+91 8122334180",R.drawable.pht5))
        list.add(CloudListRes("+91 8122337680",R.drawable.pht6))
        list.add(CloudListRes("+91 8122334780",R.drawable.pht))
        list.add(CloudListRes("+91 8122330080",R.drawable.pht2))

        cloudViewAdapter!!.setCloud(list)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }
}