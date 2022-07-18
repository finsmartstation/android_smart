package com.application.smartstation.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityAddGroupBinding
import com.application.smartstation.databinding.ActivityCreateGroupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.AddGroupAdapter
import com.application.smartstation.ui.model.DataUserList
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
import com.vanniktech.emoji.EmojiImageView
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.emoji.Emoji
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class AddGroupActivity : BaseActivity(), ImageSelectorDialog.Action {

    val binding: ActivityAddGroupBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var user = ""
    var pics = ""
    var listAdd:ArrayList<DataUserList> = ArrayList()
    var emojiPopup: EmojiPopup? = null
    var imageSelectorDialog:ImageSelectorDialog? = null
    var grpProfile:MultipartBody.Part? = null
    var addGrpAdapter: AddGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.create_grp)

        binding.rvParticipant.layoutManager = GridLayoutManager(this,4)
        addGrpAdapter = AddGroupAdapter(this)
        binding.rvParticipant.adapter = addGrpAdapter

        addGrpAdapter!!.onItemClick = { model, pos ->

        }

        if (intent != null){
            user = intent.getStringExtra("members")!!
            listAdd = intent.getSerializableExtra("list") as ArrayList<DataUserList>

            binding.txtParticipants.text = resources.getString(R.string.participant)+" "+listAdd.size

            addGrpAdapter!!.setChat(listAdd)
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
            .setOnEmojiPopupDismissListener { binding.imgEmoji.setImageResource(R.drawable.ic_smile_message_icon_use) }
            .setOnSoftKeyboardCloseListener { Log.d("TAG", "Closed soft keyboard") }
            .build(binding.edtGrpName)

    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgCamera.setOnClickListener {
            imagePermission {
                imageSelectorDialog = ImageSelectorDialog(this, this,resources.getString(R.string.profile_pht))
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
                    val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(
                        Constants.USER_ID)!!)
                    val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                    )
                    val group_name: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                        grpName
                    )
                    val members: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                        user
                    )
                    createGrp(user_id,accessToken,group_name,members,grpProfile)
                }
            }
        }
    }

    private fun createGrp(userId: RequestBody, accessToken: RequestBody, groupName: RequestBody, members: RequestBody, grpProfile: MultipartBody.Part?) {
        apiViewModel.grpCreate(userId,accessToken,groupName,members,grpProfile!!).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            finish()
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
                    Glide.with(this).load(uri).into(binding.imgCamera)
                    pics = uri.path.toString()
                    val file = File(pics)
                    val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                    grpProfile = MultipartBody.Part.createFormData("group_profile", file.getName(), requestBody)
                }
            }
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }
}