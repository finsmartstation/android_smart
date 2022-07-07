package com.application.smartstation.ui.activity

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewLetterBinding
import com.application.smartstation.util.viewBinding

class NewLetterActivity : BaseActivity() {

    val binding:ActivityNewLetterBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_letter)
        initView()
        setOnClickListener()
    }

    private fun initView() {
//        binding.ilHeader.imgHeader.visibility = View.GONE
//        binding.ilHeader.imgAudio.visibility = View.GONE
//        binding.ilHeader.imgVideo.visibility = View.GONE
        binding.ilHeader.txtHeader.text = resources.getString(R.string.new_letter)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

    }
}