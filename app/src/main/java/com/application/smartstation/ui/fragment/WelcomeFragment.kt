package com.application.smartstation.ui.fragment

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
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentWelcomeBinding
import com.application.smartstation.ui.activity.LoginActivity
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.TermAndConditionActivity
import com.application.smartstation.util.viewBinding

class WelcomeFragment : BaseFragment(R.layout.fragment_welcome) {

    private val binding by viewBinding(FragmentWelcomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        val SpanString = SpannableString(
            binding.txtTerms.text.toString()
        )

        val teremsAndCondition: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                startActivity(
                    Intent(requireActivity(), TermAndConditionActivity::class.java)
                        .putExtra("type", "terms"))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val privacy: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
//                startActivity(
//                    Intent(applicationContext, TermAndConditionActivity::class.java)
//                        .putExtra("type","privacypolicy"))

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        SpanString.setSpan(teremsAndCondition, 57, 73, 0)
        SpanString.setSpan(privacy, 9, 23, 0)
        SpanString.setSpan(RelativeSizeSpan(1f), 57, 73, 0)
        SpanString.setSpan(RelativeSizeSpan(1f), 9, 23, 0)
        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.color_dark_green)),
            57,
            73,
            0)
        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.color_dark_green)),
            9,
            23,
            0)

        binding.txtTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.txtTerms.setText(SpanString, TextView.BufferType.SPANNABLE)
        binding.txtTerms.isSelected = true

    }

    private fun setOnClickListener() {
        binding.llStart.setOnClickListener {
            (activity as LoginActivity).fragmentHelper?.push(LoginFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        loginBack = 0
    }

}