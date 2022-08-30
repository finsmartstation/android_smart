package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAccountBinding
import com.application.smartstation.databinding.ActivitySettingsBinding
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
            startActivity(Intent(this,SecurityActivity::class.java))
        }

        binding.llSignature.setOnClickListener {
            startActivity(Intent(this,SignatureActivity::class.java).putExtra("type",1))
        }

        binding.llStamp.setOnClickListener {
            startActivity(Intent(this,SignatureActivity::class.java).putExtra("type",2))
        }
    }
}