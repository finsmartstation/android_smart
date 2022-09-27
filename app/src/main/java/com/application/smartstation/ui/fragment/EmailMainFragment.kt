package com.application.smartstation.ui.fragment

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentEmailMainBinding
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.util.viewBinding

class EmailMainFragment : BaseFragment(R.layout.fragment_email_main) {

    private val binding by viewBinding(FragmentEmailMainBinding::bind)

    var fragmentHelper: FragmentHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setUpFragments()
    }

    private fun setUpFragments() {
        fragmentHelper = FragmentHelper(childFragmentManager)
        fragmentHelper?.setUpFrame(InboxFragment(), binding.flEmail)
    }


}