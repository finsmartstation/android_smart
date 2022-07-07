package com.application.smartstation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAccountBinding
import com.application.smartstation.databinding.ActivitySignatureBinding
import com.application.smartstation.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignatureActivity : BaseActivity() {

    val binding: ActivitySignatureBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        initView()
        setOnClickListener()
    }

    private fun initView() {

    }

    private fun setOnClickListener() {

    }
}