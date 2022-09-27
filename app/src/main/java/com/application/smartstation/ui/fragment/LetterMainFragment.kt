package com.application.smartstation.ui.fragment

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentLetterMainBinding
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.util.viewBinding

class LetterMainFragment : BaseFragment(R.layout.fragment_letter_main) {

    private val binding by viewBinding(FragmentLetterMainBinding::bind)

    var fragmentHelper: FragmentHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        setUpFragments()
    }

    private fun setUpFragments() {
        fragmentHelper = FragmentHelper(childFragmentManager)
        fragmentHelper?.setUpFrame(LetterInboxFragment(), binding.flLetter)
    }

    private fun setOnClickListener() {

    }

}