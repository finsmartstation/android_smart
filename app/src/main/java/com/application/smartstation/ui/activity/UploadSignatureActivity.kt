package com.application.smartstation.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySignatureBinding
import com.application.smartstation.databinding.ActivityUploadSignatureBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class UploadSignatureActivity : BaseActivity(), ImageSelectorDialog.Action {

    val binding: ActivityUploadSignatureBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var imageSelectorDialog:ImageSelectorDialog? = null
    var pics=""
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_signature)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null) {
            type = intent.getIntExtra("type", 0)
            if (type.equals(1)) {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.signature)
            } else {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.stamp)
            }
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgUpload.setOnClickListener {
            imagePermission {
                imageSelectorDialog = ImageSelectorDialog(this, this,resources.getString(R.string.signature))
            }
        }

        binding.llSubmit.setOnClickListener {
            val name = binding.edtName.text.toString().trim()

            when {
                TextUtils.isEmpty(name) -> toast(resources.getString(R.string.please_ceo))
                name.length < 3 -> toast(resources.getString(R.string.please_name_min))
                TextUtils.isEmpty(pics) -> toast(resources.getString(R.string.please_upload_sign))
                else -> {
                    if (type.equals(1)) {
                        val user_id: RequestBody =
                            RequestBody.create("text/plain".toMediaTypeOrNull(),
                                UtilsDefault.getSharedPreferenceString(
                                    Constants.USER_ID)!!)
                        val accessToken: RequestBody =
                            RequestBody.create("text/plain".toMediaTypeOrNull(),
                                UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                            )
                        val name: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            name
                        )
                        val file = File(pics)
                        val requestBody =
                            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                        val signature = MultipartBody.Part.createFormData("signature",
                            file.getName(),
                            requestBody)

                        uploadSignature(user_id, accessToken, name, signature)
                    }else{
                        val user_id: RequestBody =
                            RequestBody.create("text/plain".toMediaTypeOrNull(),
                                UtilsDefault.getSharedPreferenceString(
                                    Constants.USER_ID)!!)
                        val accessToken: RequestBody =
                            RequestBody.create("text/plain".toMediaTypeOrNull(),
                                UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                            )
                        val name: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            name
                        )
                        val file = File(pics)
                        val requestBody =
                            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                        val stamp = MultipartBody.Part.createFormData("stamp",
                            file.getName(),
                            requestBody)

                        uploadStamp(user_id, accessToken, name, stamp)
                    }
                }
            }
        }

    }

    private fun uploadStamp(userId: RequestBody, accessToken: RequestBody, name: RequestBody, stamp: MultipartBody.Part) {
        apiViewModel.stampUpload(userId,accessToken,name,stamp).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            finish()
                            toast(it.data.message)
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

    fun uploadSignature(userId: RequestBody, accessToken: RequestBody, name: RequestBody, signature: MultipartBody.Part) {
        apiViewModel.uploadSignature(userId,accessToken,name,signature).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            finish()
                            toast(it.data.message)
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
        pics = imagePath
        Glide.with(this).load(imagePath).into(binding.imgUpload)
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