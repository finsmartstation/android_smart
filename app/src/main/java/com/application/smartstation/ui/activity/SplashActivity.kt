package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySplashBinding
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding

class SplashActivity : AppCompatActivity() {

    val binding: ActivitySplashBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val w = window
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash)
        initView()
    }

    private fun initView() {
        Thread(Runnable {
            Thread.sleep(4000)
            if (UtilsDefault.isLoggedIn(this)) {
                if (UtilsDefault.getSharedPreferenceString(Constants.PASSCODE) != null && UtilsDefault.getSharedPreferenceString(
                        Constants.PASSCODE) == "yes"
                ) {
                    val intent = Intent(this, PasscodeActivity::class.java)
                    intent.putExtra("isFromSplash", true)
                    startActivity(intent)
                    finish()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }).start()
    }

}