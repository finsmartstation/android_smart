package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityPasscodeBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FingerPrintHelper
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasscodeActivity : BaseActivity() {

    val binding: ActivityPasscodeBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var isFromSplash = false
    var isFromDeposit = false
    var isFromSettings = false
    private var isConfirm = false
    private var count = 0
    private var builder: StringBuilder? = null
    private var confirmBuilder: StringBuilder? = null
    var setPinTo = 1
    var oldpin = ""
    var newpin = ""

    companion object {
        lateinit var biometricManager: BiometricManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryStatusBar()
        setContentView(R.layout.activity_passcode)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.passcode)
        biometricManager = BiometricManager.from(this)
        builder = StringBuilder()
        confirmBuilder = StringBuilder()

        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            binding.imgFingerprint.visibility = View.INVISIBLE
        } else {
            binding.imgFingerprint.visibility = View.VISIBLE
        }

        if (intent != null) {
            isFromSplash = intent.getBooleanExtra("isFromSplash", false)
            isFromSettings = intent.getBooleanExtra("isFromSettings", false)
//            isFromDeposit = intent.getBooleanExtra("isFromDeposit",false)
        }

        if (isFromSplash) {
            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                binding.imgFingerprint.visibility = View.VISIBLE
            } else {
                binding.imgFingerprint.visibility = View.INVISIBLE
            }
            setPinTo = 3
            binding.tvEnterPin.text = resources.getString(R.string.enter_pin)
//            fingerPrintAuth()
        } else {
            binding.imgFingerprint.visibility = View.INVISIBLE
        }

//        if (isFromDeposit){
//            binding.imgFingerprint.visibility = View.VISIBLE
//            setPinTo = 5
//            binding.tvEnterPin.text = "Enter Pin"
//            fingerPrintAuth()
//        }

        if (isFromSettings) {
            setPinTo = 4
            binding.tvEnterPin.text = resources.getString(R.string.deactive_passcode)
        }

    }

    private fun checkFingerPrint() {

        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            UtilsDefault.updateSharedPreferenceString(Constants.FINGERPRINT, "yes")
            finish()
        } else {

            FingerPrintHelper().scanData(this, object : FingerPrintHelper.FingerPrintAction {
                override fun onSuccess(result: String?) {

                    if (UtilsDefault.getSharedPreferenceString(Constants.FINGERPRINT) == "yes") {
                        val intent = Intent(this@PasscodeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        UtilsDefault.updateSharedPreferenceString(Constants.FINGERPRINT, "yes")
                        finish()
                    }

                }

                override fun onFailed(result: String?) {
                    clearPins()
                    builder!!.setLength(0)
                    count = 0
                    isConfirm = false
                    finish()
                    Toast.makeText(this@PasscodeActivity, result, Toast.LENGTH_SHORT).show()
                }

            })

        }

    }

    private fun clearPins() {
        binding.code1.setImageResource(R.drawable.passcode_unselect)
        binding.code2.setImageResource(R.drawable.passcode_unselect)
        binding.code3.setImageResource(R.drawable.passcode_unselect)
        binding.code4.setImageResource(R.drawable.passcode_unselect)
    }

    private fun fingerPrintAuth() {

        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {

            FingerPrintHelper().scanData(this, object : FingerPrintHelper.FingerPrintAction {
                override fun onSuccess(result: String?) {

                    if (isFromSettings) {
                        val intent = Intent()
                        intent.putExtra("status", "verified")
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val intent = Intent(this@PasscodeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }

                override fun onFailed(result: String?) {
                    Toast.makeText(this@PasscodeActivity, result, Toast.LENGTH_SHORT).show()
                }

            })
        }


    }

    private fun validatePin() {
        if (setPinTo == 1) {
            setPinTo = 2
            binding.tvEnterPin.text = resources.getString(R.string.confirm_pin)
            newpin = builder.toString()
            builder!!.setLength(0)
            count = 0
            clearPins()
            isConfirm = false
        } else if (setPinTo == 2) {

            if (!newpin.equals(builder.toString())) {

                clearPins()
                builder!!.setLength(0)
                count = 0
                isConfirm = false
                binding.llPinPage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                toast("Pin mismatch")
            } else {
                val inputParams = InputParams()
                inputParams.accessToken =
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                inputParams.security_pin = newpin
                setPin(inputParams)
            }
        } else if (setPinTo == 3) {
            newpin = builder.toString()
            val inputParams = InputParams()
            inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
            inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
            inputParams.security_pin = newpin
            checkPin(inputParams)
        } else if (setPinTo == 4) {
            newpin = builder.toString()
            val inputParams = InputParams()
            inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
            inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
            inputParams.security_pin = newpin
            removePin(inputParams)
        } else if (setPinTo == 5) {

            if (!UtilsDefault.getSharedPreferenceString(Constants.PASSCODE)
                    .equals(builder.toString())
            ) {

                clearPins()
                builder!!.setLength(0)
                count = 0
                isConfirm = false
                binding.llPinPage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                Toast.makeText(this, "Pin wrong", Toast.LENGTH_SHORT).show()

            } else {
                val intent = Intent()
                intent.putExtra("status", "verified")
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun removePin(inputParams: InputParams) {
        apiViewModel.passcodeRemove(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            UtilsDefault.updateSharedPreferenceString(Constants.PASSCODE, "no")
                            finish()
                        } else {
                            clearPins()
                            builder!!.setLength(0)
                            count = 0
                            isConfirm = false
                            binding.llPinPage.startAnimation(AnimationUtils.loadAnimation(this,
                                R.anim.shake))
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        dismissProgress()
                        toast(it.message!!)
                    }
                }
            }
        })
    }

    private fun checkPin(inputParams: InputParams) {
        apiViewModel.passcodeCheck(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            clearPins()
                            builder!!.setLength(0)
                            count = 0
                            isConfirm = false
                            binding.llPinPage.startAnimation(AnimationUtils.loadAnimation(this,
                                R.anim.shake))
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        dismissProgress()
                        toast(it.message!!)
                    }
                }
            }
        })
    }

    private fun setPin(inputParams: InputParams) {
        apiViewModel.passcodeUpdate(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            UtilsDefault.updateSharedPreferenceString(Constants.PASSCODE, "yes")
                            finish()
                        } else {
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        dismissProgress()
                        toast(it.message!!)
                    }
                }
            }
        })
    }

    fun updateimage() {

        if (count == 0) {
            binding.code1.setImageResource(R.drawable.passcode_unselect)
            binding.code2.setImageResource(R.drawable.passcode_unselect)
            binding.code3.setImageResource(R.drawable.passcode_unselect)
            binding.code4.setImageResource(R.drawable.passcode_unselect)
        } else if (count == 1) {
            binding.code1.setImageResource(R.drawable.passcode_select)
            binding.code2.setImageResource(R.drawable.passcode_unselect)
            binding.code3.setImageResource(R.drawable.passcode_unselect)
            binding.code4.setImageResource(R.drawable.passcode_unselect)
        } else if (count == 2) {
            binding.code1.setImageResource(R.drawable.passcode_select)
            binding.code2.setImageResource(R.drawable.passcode_select)
            binding.code3.setImageResource(R.drawable.passcode_unselect)
            binding.code4.setImageResource(R.drawable.passcode_unselect)
        } else if (count == 3) {
            binding.code1.setImageResource(R.drawable.passcode_select)
            binding.code2.setImageResource(R.drawable.passcode_select)
            binding.code3.setImageResource(R.drawable.passcode_select)
            binding.code4.setImageResource(R.drawable.passcode_unselect)
        } else if (count == 4) {
            binding.code1.setImageResource(R.drawable.passcode_select)
            binding.code2.setImageResource(R.drawable.passcode_select)
            binding.code3.setImageResource(R.drawable.passcode_select)
            binding.code4.setImageResource(R.drawable.passcode_select)

            val handler = Handler()
            handler.postDelayed(Runnable {
                //Do something after 100ms
                validatePin()

            }, 100)

        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgFingerprint.setOnClickListener {
            fingerPrintAuth()
        }

        binding.t0.setOnClickListener {

            if (isConfirm == false) {
                builder!!.append("0")
            } else {
                confirmBuilder!!.append("0")
            }
            count++
            updateimage()
        }

        binding.t1.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("1")
            } else {
                confirmBuilder!!.append("1")
            }
            count++
            updateimage()
        }
        binding.t2.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("2")
            } else {
                confirmBuilder!!.append("2")
            }
            count++
            updateimage()
        }

        binding.t3.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("3")
            } else {
                confirmBuilder!!.append("3")
            }
            count++
            updateimage()
        }
        binding.t4.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("4")
            } else {
                confirmBuilder!!.append("4")
            }
            count++
            updateimage()
        }
        binding.t5.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("5")
            } else {
                confirmBuilder!!.append("5")
            }
            count++
            updateimage()
        }
        binding.t6.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("6")
            } else {
                confirmBuilder!!.append("6")
            }
            count++
            updateimage()
        }
        binding.t7.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("7")
            } else {
                confirmBuilder!!.append("7")
            }
            count++
            updateimage()
        }
        binding.t8.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("8")
            } else {
                confirmBuilder!!.append("8")
            }
            count++
            updateimage()
        }
        binding.t9.setOnClickListener {
            if (isConfirm == false) {
                builder!!.append("9")
            } else {
                confirmBuilder!!.append("9")
            }
            count++
            updateimage()
        }

        binding.clearImg1.setOnClickListener {
            if (isConfirm == false) {
                if (builder!!.length > 0) {
                    count--
                    builder!!.setLength(builder!!.length - 1)
                } else {
                    count = 0
                }
            } else {
                if (confirmBuilder!!.length > 0) {
                    count--
                    confirmBuilder!!.setLength(confirmBuilder!!.length - 1)
                } else {
                    count = 0
                }
            }
            updateimage()
        }
    }
}