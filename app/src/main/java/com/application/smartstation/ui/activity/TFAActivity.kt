package com.application.smartstation.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityTfaactivityBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TFAActivity : BaseActivity() {

    val binding:ActivityTfaactivityBinding by viewBinding()
    var tfaStatus = ""
    var tfakey = ""
    var tfaUrl = ""
    val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tfaactivity)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgTfaPaste.setOnClickListener {
            val text = UtilsDefault.pastefromClipboard(this)
            binding.edtCode.setText(text)
        }

        binding.imgCopy.setOnClickListener {
            val address = binding.textTfaKey.text.toString().trim()
            UtilsDefault.copyToClipboard(this,address)
            toast("Copied to Clipboard")
        }

        binding.scTFA.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val isInstalled =
                    UtilsDefault.isAppInstalled(this, "com.google.android.apps.authenticator2")
                if (isInstalled) {
                    if (binding.scTFA.isPressed) {
                        if (tfaStatus == "" || tfaStatus == "disable") {
                            if (!TextUtils.isEmpty(tfaUrl)) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tfaUrl))
                                startActivity(intent)
                                binding.txtOnOff.text = resources.getString(R.string.on)
                            }
                        } else {
                            val pm = packageManager
                            val launchIntent =
                                pm.getLaunchIntentForPackage("com.google.android.apps.authenticator2")
                            startActivity(launchIntent)
                            binding.txtOnOff.text = resources.getString(R.string.off)
                        }

                    }

                } else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.google.android.apps.authenticator2")))
                    } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2")))
                    }
                    toast("Please install authenticator app")
                }
            }
        }

        binding.btnEnable.setOnClickListener {
            val code =  binding.edtCode.text.toString().trim()
            when{
                TextUtils.isEmpty(code) -> showEnter("TFA Code")
                code.length < 6 -> showEnter("Valid TFA Code")
                else -> {
                    val params = InputParams()
                    params.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                    params.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                    params.authenticator_pin = code
                    updateTfa(params)
                }
            }
    }
    }

    private fun updateTfa(params: InputParams) {
        apiViewModel.updateTFA(params).observe(this, Observer {
            it?.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (it.data.tfa_status.equals("1")){
                                UtilsDefault.updateSharedPreferenceInt(Constants.TFA_STATUS,1)
                                toast("TFA enabled successfully")
                            }else{
                                UtilsDefault.updateSharedPreferenceInt(Constants.TFA_STATUS,0)
                                toast("TFA disabled successfully")
                            }
                            finish()
                        }
                        else {
                            toast("Invalid 2FA Code")
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

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.tfa)

        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        getTfaStatus(inputParams)
    }

    private fun getTfaStatus(inputParams: InputParams) {
        apiViewModel.getTFA(inputParams).observe(this, Observer {
            it?.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            UtilsDefault.updateSharedPreferenceInt(Constants.TFA_STATUS,  it.data.tfa_status.toInt())
                                setData(it.data.secret_key, it.data.tfa_status.toInt())
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

    private fun setData(secretKey: String, tfastatus: Int) {
        tfakey = secretKey
        binding.textTfaKey.text = tfakey
        val name = UtilsDefault.getSharedPreferenceString(Constants.NAME)
        tfaUrl = "otpauth://totp/${resources.getString(R.string.app_name)}($name)?secret=$secretKey"
        setQrCode(secretKey)
        if (tfastatus == 1){
            binding.txtOnOff.text = resources.getString(R.string.on)
            binding.scTFA.isChecked = true
            binding.scTFA.isEnabled = false
            tfaStatus = "enable"
            binding.txtBtn.text = resources.getString(R.string.disable)
        }
        else {
            binding.txtOnOff.text = resources.getString(R.string.off)
            binding.scTFA.isChecked = false
            tfaStatus = "disable"
            binding.scTFA.isEnabled = true
            binding.txtBtn.text = resources.getString(R.string.enable)
        }
    }

    private fun showEnter(fieldName: String){
        toast("Enter $fieldName!")
    }

    private fun setQrCode(secret: String) {
        val multiFormatWriter = QRCodeWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(secret, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding.imgTFAQRCode.setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}