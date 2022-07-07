package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAccountBinding
import com.application.smartstation.databinding.ActivitySecurityBinding
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FingerPrintHelper
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding

class SecurityActivity : BaseActivity() {

    val binding: ActivitySecurityBinding by viewBinding()

    companion object {
        lateinit var biometricManager: BiometricManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        biometricManager = BiometricManager.from(this)

        binding.ilHeader.txtHeader.text = resources.getString(R.string.security)

        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE){
            binding.llFingerPrint.visibility = View.GONE
        }
        else {
            binding.llFingerPrint.visibility = View.GONE
            binding.scFingerprint.isChecked = UtilsDefault.getSharedPreferenceString(Constants.FINGERPRINT) !=null && UtilsDefault.getSharedPreferenceString(Constants.FINGERPRINT) == "yes"
        }

    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.scPasscode.setOnCheckedChangeListener { p0, p1 ->
            if (p0.isChecked && p0.isPressed){
                val intent = Intent(this,PasscodeActivity::class.java)
                intent.putExtra("isFromSettings",false)
                startActivity(intent)
            }
            else if (!p0.isChecked && p0.isPressed) {
                val intent = Intent(this,PasscodeActivity::class.java)
                intent.putExtra("isFromSettings",true)
                startActivity(intent)
            }

        }

        binding.llTfa.setOnClickListener {
            startActivity(Intent(this,TFAActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        binding.scPasscode.isChecked = UtilsDefault.getSharedPreferenceString(Constants.PASSCODE) !=null && UtilsDefault.getSharedPreferenceString(Constants.PASSCODE) == "yes"
    }
}