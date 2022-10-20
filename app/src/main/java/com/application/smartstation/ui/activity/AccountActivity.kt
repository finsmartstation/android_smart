package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAccountBinding
import com.application.smartstation.util.viewBinding

class AccountActivity : BaseActivity() {

    val binding: ActivityAccountBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.account)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.llSecurity.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
        }

        binding.llPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        binding.llSignature.setOnClickListener {
            startActivity(Intent(this, SignatureActivity::class.java).putExtra("type", 1))
        }

        binding.llLetterHeader.setOnClickListener {
            startActivity(Intent(this, SignatureActivity::class.java).putExtra("type", 3))
        }

        binding.llLetterFooter.setOnClickListener {
            startActivity(Intent(this, SignatureActivity::class.java).putExtra("type", 4))
        }

        binding.llStamp.setOnClickListener {
            startActivity(Intent(this, SignatureActivity::class.java).putExtra("type", 2))
        }
    }
}