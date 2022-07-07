package com.application.smartstation.ui.activity

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityLetterViewBinding
import com.application.smartstation.util.Constants
import com.application.smartstation.util.viewBinding
import com.bumptech.glide.Glide

class LetterViewActivity : BaseActivity() {

    private val binding:ActivityLetterViewBinding by viewBinding()

    var name = ""
    var profile = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_letter_view)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun initView() {

        if (intent != null){
            binding.ilHeader.txtHeader.text = intent.getStringExtra(Constants.NAME)
            Glide.with(this).load(intent.getIntExtra(Constants.PROFILE, 0))
                .into(binding.ilHeader.imgHeader)
        }

        binding.ilHeader.imgAudio.visibility = View.GONE
        binding.ilHeader.imgVideo.visibility = View.GONE

    }
}