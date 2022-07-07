package com.application.smartstation.ui.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentLostMailBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.LoginActivity
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LostMailFragment : BaseFragment(R.layout.fragment_lost_mail) {

    private val binding by viewBinding(FragmentLostMailBinding::bind)
    val apiViewModel: ApiViewModel by viewModels()
    var email=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        val SpanString = SpannableString(
            binding.txtLostDec.text.toString()
        )

        val resendOtp: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }


        SpanString.setSpan(resendOtp, 18, 31, 0)
        SpanString.setSpan(RelativeSizeSpan(1f), 18, 31, 0)
        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 18, 31, 0)

        binding.txtLostDec.setMovementMethod(LinkMovementMethod.getInstance())
        binding.txtLostDec.setText(SpanString, TextView.BufferType.SPANNABLE)
        binding.txtLostDec.setSelected(true)

    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            (activity as LoginActivity).fragmentHelper?.push(LoginFragment())
        }

        binding.llSubmit.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(requireActivity())
            email = binding.edtEmail.text.toString().trim()

            when {

                TextUtils.isEmpty(email) -> toast(requireActivity().resources.getString(R.string.please_enter_mail))
                !UtilsDefault.isEmailValid(email) -> toast(requireActivity().resources.getString(R.string.please_enter_valid_mail))

                else -> {
                    val inputParams = InputParams()
                    inputParams.mail_id = email
                    sendOtpMail(inputParams)
                }

            }
        }
    }

    private fun sendOtpMail(inputParams: InputParams) {
        apiViewModel.sendMailOTP(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            val lostOTPFragment = LostOTPFragment()
                            val bundle = Bundle()
                            val email = binding.edtEmail.text.toString().trim()
                            bundle.putString(Constants.MAIL_ID, email)
                            lostOTPFragment.arguments = bundle
                            (activity as LoginActivity).fragmentHelper?.push((lostOTPFragment))
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

    override fun onResume() {
        super.onResume()
        loginBack = 1
        binding.ilHeader.txtHeader.text = requireActivity().getString(R.string.lost_phn)
    }

}