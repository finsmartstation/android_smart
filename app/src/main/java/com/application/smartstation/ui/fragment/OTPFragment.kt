package com.application.smartstation.ui.fragment

import `in`.aabhasjindal.otptextview.OTPListener
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentOTPBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.LoginActivity
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.ui.activity.PasscodeActivity
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.RetrievalEvent
import com.application.smartstation.util.Constants
import com.application.smartstation.util.SignatureHelper
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class OTPFragment : BaseFragment(R.layout.fragment_o_t_p) {

    private val binding by viewBinding(FragmentOTPBinding::bind)

    val apiViewModel: ApiViewModel by viewModels()

    var mobile:String? = null
    var countryNum:String? = null
    var otpData:String? = null
    private lateinit var smsClient: SmsRetrieverClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.txtWrong.setOnClickListener {
            (activity as LoginActivity).fragmentHelper?.push(LoginFragment())
        }

        binding.btnVerify.setOnClickListener {
            if (binding.otpView.otp!!.length.equals(6)){
                completeOTP(otpData!!)
            }
        }
    }

    private fun initView() {

        smsClient = SmsRetriever.getClient(requireActivity())

        val appSignatureHelper = SignatureHelper(requireActivity())
        Log.d("SIGNATURE",appSignatureHelper.appSignature.toString())

        val bundle = arguments
        bundle?.let {
            mobile = it.getString(Constants.MOB)
            countryNum = it.getString(Constants.COUNTRY_NUM)
            otpData = it.getInt(Constants.OTP,0).toString()
            binding.txtNumber.text = countryNum+" "+mobile+". "
        }

        binding.otpView.setOTP(otpData!!)

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                completeOTP(otp)
            }

        }

        val SpanString = SpannableString(
            binding.txtResend.text.toString()
        )

        val resendOtp: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                var inputParams = InputParams()
                inputParams.phone = mobile
                resendOTP(inputParams)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }


        SpanString.setSpan(resendOtp, 28, 34, 0)
        SpanString.setSpan(RelativeSizeSpan(1f), 28, 34, 0)
        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.color_dark_green)), 28, 34, 0)

        binding.txtResend.setMovementMethod(LinkMovementMethod.getInstance())
        binding.txtResend.setText(SpanString, TextView.BufferType.SPANNABLE)
        binding.txtResend.setSelected(true)

        //initSmsListener()

    }

    private fun resendOTP(inputParams: InputParams) {
        apiViewModel.resendOTP(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            binding.otpView.setOTP(otpData!!)
                        }else{
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

    private fun initSmsListener() {

        smsClient.startSmsRetriever()
            .addOnSuccessListener {
                /*Toast.makeText(
                    requireActivity(), "Waiting for sms message",
                    Toast.LENGTH_SHORT
                ).show()*/
            }.addOnFailureListener { failure ->
                Toast.makeText(
                    requireActivity(), failure.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    @Subscribe
    fun onReceiveSms(retrievalEvent: RetrievalEvent) {
        val code: String =
            StringUtils.substringAfterLast(retrievalEvent.message, "is").replace(":", "")
                .trim().substring(0, 6)

        requireActivity().runOnUiThread {
            if (!retrievalEvent.timedOut) {
                binding.otpView.setOTP(code)
            } else {
                Toast.makeText(requireActivity(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }
        initSmsListener()
    }

    private fun completeOTP(otp: String) {
        UtilsDefault.hideKeyboardForFocusedView(requireActivity())
        var inputParams = InputParams()
        inputParams.otp = otp
        verifyOTP(inputParams)
    }

    private fun verifyOTP(inputParams: InputParams) {
        apiViewModel.otpVerify(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            binding.otpView.showSuccess()
                           toast(it.data.message)
                            UtilsDefault.updateSharedPreferenceString(Constants.USER_ID,it.data.data.id)
                            UtilsDefault.updateSharedPreferenceString(Constants.ACCESS_TOKEN,it.data.data.accessToken)
                            if(it.data.data.login_status.equals("0")) {
                                (activity as LoginActivity).fragmentHelper?.push(ProfileFragment())
                            }else{
                                UtilsDefault.updateSharedPreferenceString(Constants.PROFILE_PIC,it.data.data.profile_pic)
                                UtilsDefault.updateSharedPreferenceString(Constants.NAME,it.data.data.name)
                                UtilsDefault.updateSharedPreferenceString(Constants.ABOUT,it.data.data.about)
                                UtilsDefault.setLoggedIn(requireActivity(),true)
                                if(it.data.data.security_status.equals("1")) {
                                    UtilsDefault.updateSharedPreferenceString(Constants.PASSCODE,"yes")
                                    val intent = Intent(requireActivity(), PasscodeActivity::class.java)
                                    intent.putExtra("isFromSplash",true)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }else{
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            MainActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                }
                            }
                        }else{
                            binding.otpView.showError()
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

    override fun onResume() {
        super.onResume()
        loginBack = 1
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}