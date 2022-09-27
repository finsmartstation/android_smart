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
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentLostOTPBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.LoginActivity
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.ui.activity.PasscodeActivity
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LostOTPFragment : BaseFragment(R.layout.fragment_lost_o_t_p) {

    private val binding by viewBinding(FragmentLostOTPBinding::bind)
    val apiViewModel: ApiViewModel by viewModels()
    var mail: String? = null
    var otpData: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        val bundle = arguments
        bundle?.let {
            mail = it.getString(Constants.MAIL_ID)
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
            binding.txtVerificationDec.text.toString()
        )

        val s: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        SpanString.setSpan(s, 50, 63, 0)
        SpanString.setSpan(RelativeSizeSpan(1f), 50, 63, 0)
        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 50, 63, 0)

        binding.txtVerificationDec.movementMethod = LinkMovementMethod.getInstance()
        binding.txtVerificationDec.setText(SpanString, TextView.BufferType.SPANNABLE)
        binding.txtVerificationDec.isSelected = true

        val SpanString1 = SpannableString(
            binding.txtResend.text.toString()
        )

        val resendOtp: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val inputParams = InputParams()
                inputParams.mail_id = mail
                resendOTP(inputParams)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        SpanString1.setSpan(resendOtp, 28, 34, 0)
        SpanString1.setSpan(RelativeSizeSpan(1f), 28, 34, 0)
        SpanString1.setSpan(ForegroundColorSpan(resources.getColor(R.color.color_dark_green)),
            28,
            34,
            0)

        binding.txtResend.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResend.setText(SpanString1, TextView.BufferType.SPANNABLE)
        binding.txtResend.isSelected = true

    }

    private fun resendOTP(inputParams: InputParams) {
        apiViewModel.resendEmailOTP(inputParams).observe(requireActivity(), Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
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

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            (activity as LoginActivity).fragmentHelper?.push(LostMailFragment())
        }
    }

    private fun completeOTP(otp: String) {
        UtilsDefault.hideKeyboardForFocusedView(requireActivity())
        val inputParams = InputParams()
        inputParams.otp = otp
        verifyOTP(inputParams)
    }

    private fun verifyOTP(inputParams: InputParams) {
        apiViewModel.checkEmailOTP(inputParams).observe(requireActivity(), Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            binding.otpView.showSuccess()
                            toast(it.data.message)
                            UtilsDefault.updateSharedPreferenceString(Constants.USER_ID,
                                it.data.data.id)
                            UtilsDefault.updateSharedPreferenceString(Constants.ACCESS_TOKEN,
                                it.data.data.accessToken)
                            if (it.data.data.login_status.equals("0")) {
                                (activity as LoginActivity).fragmentHelper?.push(ProfileFragment())
                            } else {
                                UtilsDefault.updateSharedPreferenceString(Constants.PROFILE_PIC,
                                    it.data.data.profile_pic)
                                UtilsDefault.updateSharedPreferenceString(Constants.NAME,
                                    it.data.data.name)
                                UtilsDefault.updateSharedPreferenceString(Constants.ABOUT,
                                    it.data.data.about)
                                UtilsDefault.setLoggedIn(requireActivity(), true)
                                if (it.data.data.security_status.equals("1")) {
                                    UtilsDefault.updateSharedPreferenceString(Constants.PASSCODE,
                                        "yes")
                                    val intent =
                                        Intent(requireActivity(), PasscodeActivity::class.java)
                                    intent.putExtra("isFromSplash", true)
                                    startActivity(intent)
                                    requireActivity().finish()
                                } else {
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            MainActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                }
                            }
                        } else {
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
        loginBack = 3
        binding.ilHeader.txtHeader.text = requireActivity().getString(R.string.verification)
    }

}