package com.application.smartstation.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentProfileBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile),ImageSelectorDialog.Action {

    private val binding by viewBinding(FragmentProfileBinding::bind)
    val apiViewModel:ApiViewModel by viewModels()
    var imageSelectorDialog:ImageSelectorDialog? = null
    var pics=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.imgProfile.setOnClickListener {
            imagePermission {
                imageSelectorDialog = ImageSelectorDialog(this, this,requireActivity().getString(R.string.profile_pht))
            }
        }

        binding.llSubmit.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val profilePic = pics

            when {
                TextUtils.isEmpty(name) -> toast(requireActivity().resources.getString(R.string.please_ceo))
                name.length < 3 -> toast(requireActivity().resources.getString(R.string.please_name_min))
                TextUtils.isEmpty(profilePic) -> toast(requireActivity().resources.getString(R.string.please_upload_profile))

                else -> {
                    val file = File(pics)
                    val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                    val profile = MultipartBody.Part.createFormData("profile_pic", file.getName(), requestBody)

                    val name1: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
                    val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!)
                    val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                    )

                    updateProfile(user_id,accessToken,name1,profile)
                }
            }
        }
    }

    private fun updateProfile(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        profile: MultipartBody.Part
    ) {
        apiViewModel.updateProfile(user_id,accessToken,name,profile).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            UtilsDefault.updateSharedPreferenceString(Constants.PROFILE_PIC,it.data.data.profile_pic)
                            UtilsDefault.updateSharedPreferenceString(Constants.NAME,it.data.data.name)
                            UtilsDefault.updateSharedPreferenceString(Constants.ABOUT,it.data.data.about)
                            UtilsDefault.setLoggedIn(requireActivity(),true)
                            startActivity(Intent(requireActivity(),MainActivity::class.java))
                            activity?.finish()
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

    private fun initView() {

    }

    override fun onResume() {
        super.onResume()
        loginBack = 1
    }

    override fun onImageSelected(imagePath: String, filename: String) {
        val file = File(imagePath)
        crop(Uri.fromFile(file))
    }

    fun crop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1,1)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .setAutoZoomEnabled(true)
            .start(requireContext(),this)
    }

//    private fun uploadImg(profileImage: MultipartBody.Part) {
//        apiViewModel.imgUpload(profileImage).observe(requireActivity(), Observer {
//            it.let {
//                when(it.status){
//                    Status.LOADING -> {
//                        showProgress()
//                    }
//                    Status.SUCCESS -> {
//                        dismissProgress()
//                        if (it.data!!.status){
//                            Log.d("TAG", "uploadImg: "+it.data.imageurl)
//                            pics = it.data.imageurl
//                            Glide.with(requireActivity()).applyDefaultRequestOptions(
//                                    RequestOptions()
//                                        .placeholder(R.drawable.profile)
//                                        .error(R.drawable.profile)
//                                    ).load(it.data.imageurl).into(binding.imgProfile)
//                        }else{
//                            toast(it.data.message)
//                        }
//                    }
//                    Status.ERROR -> {
//                        dismissProgress()
//                        toast(it.message!!)
//                    }
//                }
//            }
//        })
//    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    val uri = result.getUri()
                    Glide.with(requireActivity()).load(uri).into(binding.imgProfile)
                    pics = uri.path.toString()
                }
            }
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

}