package com.application.smartstation.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityEditProfileBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class EditProfileActivity : BaseActivity() , ImageSelectorDialog.Action{

    val binding: ActivityEditProfileBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var imageSelectorDialog:ImageSelectorDialog? = null
    var pics=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.ilHeader.txtHeader.text = resources.getString(R.string.edit_profile)

        val inputParams= InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)

        getProfile(inputParams)
    }

    private fun getProfile(inputParams: InputParams) {
        apiViewModel.getProfile(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            Glide.with(this).applyDefaultRequestOptions(
                                RequestOptions()
                                    .error(R.drawable.profile)
                            ).load(it.data.data.profile_pic).diskCacheStrategy(
                                DiskCacheStrategy.DATA).into(binding.imgProfile)
                            binding.edtName.setText(it.data.data.name)
                            binding.edtEmail.setText(it.data.data.email)
                            binding.edtAbout.setText(it.data.data.about)

                            UtilsDefault.updateSharedPreferenceString(Constants.PROFILE_PIC,it.data.data.profile_pic)
                            UtilsDefault.updateSharedPreferenceString(Constants.NAME,it.data.data.name)
                            UtilsDefault.updateSharedPreferenceString(Constants.ABOUT,it.data.data.about)
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

    private fun setOnClickListener() {

        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgProfile.setOnClickListener {
            imagePermission {
                imageSelectorDialog = ImageSelectorDialog(this, this,resources.getString(R.string.profile_pht))
            }
        }

        binding.llSubmit.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val about = binding.edtAbout.text.toString().trim()

            when {
                TextUtils.isEmpty(name) -> toast(resources.getString(R.string.please_ceo))
                name.length < 3 -> toast(resources.getString(R.string.please_name_min))
                TextUtils.isEmpty(email) -> toast(resources.getString(R.string.please_enter_mail))
                !UtilsDefault.isEmailValid(email) -> toast(resources.getString(R.string.please_enter_valid_mail))
                TextUtils.isEmpty(about) -> toast(resources.getString(R.string.please_enter_about))
                about.length < 3 -> toast(resources.getString(R.string.please_about_min))

                else -> {
                    val inputParams = InputParams()
                    inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                    inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                    inputParams.name = name
                    inputParams.email = email
                    inputParams.about = about
                    editProfile(inputParams)
                }
            }
        }

    }

    private fun editProfile(inputParams: InputParams) {
        apiViewModel.editProfile(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            binding.edtName.setText(it.data.name)
                            binding.edtEmail.setText(it.data.email)
                            binding.edtAbout.setText(it.data.about)
                            UtilsDefault.updateSharedPreferenceString(Constants.NAME,it.data.name)
                            UtilsDefault.updateSharedPreferenceString(Constants.ABOUT,it.data.about)
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
            .start(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    val uri = result.getUri()
                    pics = uri.path.toString()
                    val file = File(pics)
                    val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                    val profile = MultipartBody.Part.createFormData("profile_pic", file.getName(), requestBody)
                    updatePic(profile)
                }
            }
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun updatePic(profile: MultipartBody.Part) {
        val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!)
        val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
        )

        updateProfilePic(user_id,accessToken,profile)
    }

    private fun updateProfilePic(
        user_id: RequestBody,
        accessToken: RequestBody,
        profile: MultipartBody.Part
    ) {
        apiViewModel.updateProfilePic(user_id,accessToken,profile).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            Glide.with(this).applyDefaultRequestOptions(
                                RequestOptions()
                                    .error(R.drawable.profile)
                            ).load(it.data.profile_pic).diskCacheStrategy(
                                DiskCacheStrategy.DATA).into(binding.imgProfile)

                            UtilsDefault.updateSharedPreferenceString(Constants.PROFILE_PIC,it.data.profile_pic)
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
}