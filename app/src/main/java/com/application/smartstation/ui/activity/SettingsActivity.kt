package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityLoginBinding
import com.application.smartstation.databinding.ActivitySettingsBinding
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class SettingsActivity : BaseActivity() {

    val binding: ActivitySettingsBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initView()
        setOnClickListener()
    }

    private fun initView() {

    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.llProfile.setOnClickListener {
            startActivity(Intent(this,EditProfileActivity::class.java))
        }

        binding.llAcc.setOnClickListener {
            startActivity(Intent(this,AccountActivity::class.java))
        }

        binding.llNotification.setOnClickListener {
            startActivity(Intent(this,NotificationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        binding.ilHeader.txtName.text = UtilsDefault.getSharedPreferenceString(Constants.NAME)

        Glide.with(this).applyDefaultRequestOptions(
            RequestOptions()
                .error(R.drawable.profile)
        ).load(UtilsDefault.getSharedPreferenceString(Constants.PROFILE_PIC)).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.ilHeader.imgProfilePic)

    }
}