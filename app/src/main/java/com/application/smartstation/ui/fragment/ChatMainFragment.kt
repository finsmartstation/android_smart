package com.application.smartstation.ui.fragment

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentChatMainBinding
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.util.viewBinding

class ChatMainFragment : BaseFragment(R.layout.fragment_chat_main) {

    val binding by viewBinding(FragmentChatMainBinding::bind)

    var fragmentHelper: FragmentHelper? = null

    val chatFragment = ChatFragment()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setUpFragments()
        setOnClickListener()
    }

    private fun initView() {

    }

    private fun setUpFragments() {
        fragmentHelper = FragmentHelper(childFragmentManager)
        fragmentHelper?.setUpFrame(chatFragment, binding.flChat)
    }

    private fun setOnClickListener() {
        binding.rgChat.setOnCheckedChangeListener { p0, p1 ->
            when(p1){
                R.id.rbChat -> {
                    fragmentHelper?.push(ChatFragment())
                }
                R.id.rbContact -> {
                    fragmentHelper?.push(ContactChatFragment())
                }
//                R.id.rbChat -> {
//                    fragmentHelper?.push(ChatFragment())
//                }

            }
        }
    }
}