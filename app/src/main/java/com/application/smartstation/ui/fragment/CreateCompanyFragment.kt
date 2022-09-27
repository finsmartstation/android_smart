package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentCreateCompanyBinding
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.bumptech.glide.Glide

class CreateCompanyFragment : BaseFragment(R.layout.fragment_create_company),
    ImageSelectorDialog.Action {

    private val binding by viewBinding(FragmentCreateCompanyBinding::bind)
    var imageSelectorDialog: ImageSelectorDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

    }

    private fun setOnClickListener() {
        binding.imgProfile.setOnClickListener {
            imagePermission {
                imageSelectorDialog = ImageSelectorDialog(this, this, "")
            }
        }

        binding.fabProfile.setOnClickListener {
            val name = binding.edtName.text.toString().trim()

            when {

                TextUtils.isEmpty(name) -> toast(requireActivity().resources.getString(R.string.please_ceo))
                name.length < 3 -> toast(requireActivity().resources.getString(R.string.please_name_min))

                else -> {
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    activity?.finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loginBack = 2
    }

    override fun onImageSelected(imagePath: String, filename: String) {
        Glide.with(requireActivity()).load(imagePath).into(binding.imgProfile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

}