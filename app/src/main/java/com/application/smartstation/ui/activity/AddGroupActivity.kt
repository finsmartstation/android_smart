package com.application.smartstation.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAddGroupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.AddGroupAdapter
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vanniktech.emoji.EmojiImageView
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.emoji.Emoji
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.File


@AndroidEntryPoint
class AddGroupActivity : BaseActivity(), ImageSelectorDialog.Action {

    val binding: ActivityAddGroupBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var user = ""
    var pics = ""
    var listAdd: ArrayList<DataUserList> = ArrayList()
    var emojiPopup: EmojiPopup? = null
    var imageSelectorDialog: ImageSelectorDialog? = null
    var grpProfile: MultipartBody.Part? = null
    var addGrpAdapter: AddGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.create_grp)

        binding.rvParticipant.layoutManager = GridLayoutManager(this, 4)
        addGrpAdapter = AddGroupAdapter(this)
        binding.rvParticipant.adapter = addGrpAdapter

        addGrpAdapter!!.onItemClick = { model, pos ->

        }

        if (intent != null) {
            user = intent!!.getStringExtra("members")!!
            listAdd = CreateGroupActivity.listAdd
            binding.txtParticipants.text =
                resources.getString(R.string.participant) + " " + listAdd.size
            addGrpAdapter!!.setChat(listAdd, false)
        }

        emojiPopup = EmojiPopup.Builder.fromRootView(binding.clAddGrp)
            .setOnEmojiBackspaceClickListener { ignore: View? ->
                Log.d(
                    "TAG",
                    "Clicked on Backspace"
                )
            }
            .setOnEmojiClickListener { ignore: EmojiImageView?, ignore2: Emoji? ->
                Log.d(
                    "TAG",
                    "Clicked on emoji"
                )
            }
            .setOnEmojiPopupShownListener { binding.imgEmoji.setImageResource(R.drawable.ic_keyboard) }
            .setOnSoftKeyboardOpenListener { ignore: Int ->
                Log.d(
                    "TAG",
                    "Opened soft keyboard"
                )
            }
            .setOnEmojiPopupDismissListener { binding.imgEmoji.setImageResource(R.drawable.ic_smile_use) }
            .setOnSoftKeyboardCloseListener { Log.d("TAG", "Closed soft keyboard") }
            .build(binding.edtGrpName)

    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgCamera.setOnClickListener {
            imagePermission {
                imageSelectorDialog =
                    ImageSelectorDialog(this, this, resources.getString(R.string.profile_pht))
            }
        }

        binding.imgEmoji.setOnClickListener {
            emojiPopup!!.toggle()
        }

        binding.imgTick.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)
            val grpName = binding.edtGrpName.text.toString().trim()

            when {
                TextUtils.isEmpty(grpName) -> toast(resources.getString(R.string.please_enter_grp_name))
                else -> {
                    val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(
                        Constants.USER_ID)!!.toRequestBody("text/plain".toMediaTypeOrNull())
                    val accessToken: RequestBody =
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    val group_name: RequestBody =
                        grpName
                            .toRequestBody("text/plain".toMediaTypeOrNull())

                    val member = "[" + user + "]"
                    val jsonArray = JSONArray(member)
                    val strArr = arrayOfNulls<String>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        strArr[i] = jsonArray.getString(i)
                    }
//                    val members: Array<String?> = strArr
                    val members: RequestBody = user
                        .toRequestBody("text/plain".toMediaTypeOrNull())
//                    Log.d("TAG", "setOnClickListener: "+ Arrays.toString(members))
                    if (pics != null && pics != "") {
                        val file = File(pics)
                        val requestBody =
                            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        grpProfile = MultipartBody.Part.createFormData("group_profile",
                            file.name,
                            requestBody)
                    } else {
                        val attachmentEmpty: RequestBody =
                            "".toRequestBody("text/plain".toMediaTypeOrNull())
                        grpProfile =
                            MultipartBody.Part.createFormData("group_profile", "", attachmentEmpty)
                    }
                    Log.d("TAG", "setOnClickListener: " + pics)
                    createGrp(user_id, accessToken, group_name, members, grpProfile)
                }
            }
        }
    }

    private fun createGrp(
        userId: RequestBody,
        accessToken: RequestBody,
        groupName: RequestBody,
        members: RequestBody,
        grpProfile: MultipartBody.Part?,
    ) {
        apiViewModel.grpCreate(userId, accessToken, groupName, members, grpProfile!!)
            .observe(this, Observer {
                it.let {
                    when (it.status) {
                        Status.LOADING -> {
                            showProgress()
                        }
                        Status.SUCCESS -> {
                            dismissProgress()
                            if (it.data!!.status) {
                                toast(it.data.message)
                                CreateGroupActivity.listAdd.clear()
                                startActivity(Intent(this, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                                finish()
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

    override fun onImageSelected(imagePath: String, filename: String) {
        val file = File(imagePath)
        crop(Uri.fromFile(file))
    }

    fun crop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
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
                    val uri = result.uri
                    Glide.with(this).load(uri).into(binding.imgCamera)
                    pics = uri.path.toString()
                }
            }
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }
}