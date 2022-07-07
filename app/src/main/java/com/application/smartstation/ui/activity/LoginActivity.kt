package com.application.smartstation.ui.activity

import android.os.Bundle
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityLoginBinding
import com.application.smartstation.ui.fragment.LoginFragment
import com.application.smartstation.ui.fragment.LostMailFragment
import com.application.smartstation.ui.fragment.ProfileFragment
import com.application.smartstation.ui.fragment.WelcomeFragment
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity() {

    val binding: ActivityLoginBinding by viewBinding()

    var fragmentHelper: FragmentHelper? = null

    companion object {
        var loginBack = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUpFragments()
    }

    private fun setUpFragments() {
            fragmentHelper = FragmentHelper(supportFragmentManager)
            fragmentHelper?.setUpFrame(WelcomeFragment(),binding.flContainer)
    }

    override fun onBackPressed() {

        when (loginBack) {
            1 -> {
                fragmentHelper?.push(LoginFragment())
            }
            2 -> {
                fragmentHelper?.push(ProfileFragment())
            }
            3 -> {
                fragmentHelper?.push(LostMailFragment())
            }
            else -> {
                super.onBackPressed()
            }
        }



    }
}