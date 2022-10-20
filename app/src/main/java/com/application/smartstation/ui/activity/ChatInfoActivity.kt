package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.*
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.UserListGrpAdapter
import com.application.smartstation.ui.model.ContactListRes
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.UserListGrp
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.ImageSelectorDialog
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_chat_info.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class ChatInfoActivity : BaseActivity(), ImageSelectorDialog.Action {

    val binding: ActivityChatInfoBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var name = ""
    var profilePic = ""
    var chatType = ""
    var room = ""
    var desGrp = ""
    var adminID = ""
    var contactList: ArrayList<ContactListRes> = ArrayList()
    var grpUserList: ArrayList<UserListGrp> = ArrayList()
    var mBottomDialogDocument: BottomSheetDialog? = null
    var mBottomDialogDes: BottomSheetDialog? = null
    var imageSelectorDialog: ImageSelectorDialog? = null
    var pics = ""
    var imageView:ImageView? = null
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    companion object {
        var list = ArrayList<UserListGrp>()
    }

    var userListGrpAdapter: UserListGrpAdapter? = null
    var more = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        if (intent != null) {
            name = intent.getStringExtra(Constants.NAME)!!
            profilePic = intent.getStringExtra(Constants.PROFILE_PIC)!!

            if (intent.getStringExtra(Constants.CHAT_TYPE) != null) {
                chatType = intent.getStringExtra(Constants.CHAT_TYPE)!!
            } else {
                chatType = "private"
            }

            if (intent.getStringExtra(Constants.ROOM) != null) {
                room = intent.getStringExtra(Constants.ROOM)!!
            }

            binding.txtName.text = name
            Glide.with(this).load(profilePic).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA).into(binding.imgProfile)
        }

        binding.rvChatInfo.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        userListGrpAdapter = UserListGrpAdapter(this)
        binding.rvChatInfo.adapter = userListGrpAdapter
        binding.rvChatInfo.setHasFixedSize(false)

        userListGrpAdapter!!.onItemClick = { model ->
            for (i in 0 until grpUserList.size) {
                if (UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                        .equals(grpUserList[i].user_id)
                ) {
                    if (grpUserList[i].type.equals("admin")) {
                        openAdminPopup(model)
                    }else{
                        val intent = Intent(this,PrivateChatInfoActivity::class.java)
                        intent.putExtra(Constants.REC_ID,model.user_id)
                        startActivity(intent)
                    }
                }
            }

        }


        binding.appBarLayout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    //when collapsingToolbar at that time display actionbar title
                    binding.toolbar.txtHeader.text = name
                    isShow = true
                } else if (isShow) {
                    if (chatType.equals("private")) {
                        binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
                    } else {
                        binding.toolbar.txtHeader.text = resources.getString(R.string.group_info)
                    }
                    isShow = false
                }
            }
        })

//        phnPermission {
//            getContactList()
//        }
    }

    private fun openAdminPopup(model: UserListGrp) {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_admin_grp, null)
            val dialogLogout = Dialog(this)
            val window: Window = dialogLogout.window!!
            window.setGravity(Gravity.CENTER)
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.FILL_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val bind = DialogAdminGrpBinding.bind(view)
            dialogLogout.setCancelable(true)

            dialogLogout.setContentView(view)
            dialogLogout.show()

            bind.txtMsg.text = resources.getString(R.string.message)+" "+model.username
            bind.txtView.text = resources.getString(R.string.view)+" "+model.username
            bind.txtRemove.text = resources.getString(R.string.remove)+" "+model.username

            bind.txtGrpAdmin.setOnClickListener {
                dialogLogout.dismiss()
                val inputParams = InputParams()
                inputParams.accessToken =
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                inputParams.group_id = room
                inputParams.new_admin_user_id = model.user_id
                addGrpAdmin(inputParams)
            }

           bind.txtMsg.setOnClickListener {
               dialogLogout.dismiss()
               val intent = Intent(this,ChatViewActivity::class.java)
               intent.putExtra(Constants.REC_ID,model.user_id)
               intent.putExtra(Constants.NAME,model.username)
               intent.putExtra(Constants.PROFILE,model.profile_pic)
               startActivity(intent)
           }

            bind.txtView.setOnClickListener {
               dialogLogout.dismiss()
               val intent = Intent(this,PrivateChatInfoActivity::class.java)
               intent.putExtra(Constants.REC_ID,model.user_id)
               startActivity(intent)
           }

            bind.txtRemove.setOnClickListener {
                dialogLogout.dismiss()
                val inputParams = InputParams()
                inputParams.accessToken =
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                inputParams.group_id = room
                inputParams.remove_user_id = model.user_id
                removeGrp(inputParams)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeGrp(inputParams: InputParams) {
        apiViewModel.removeGrpUser(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            getUserListGrp()
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

    private fun addGrpAdmin(inputParams: InputParams) {
        apiViewModel.addGrpAdmin(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            getUserListGrp()
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

    private fun getUserListGrp() {

        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.group_id = room

        apiViewModel.getGrpUserList(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            name = it.data.group_name
                            profilePic = it.data.group_profile
                            binding.txtName.text = it.data.group_name
                            if (it.data.description.isNullOrEmpty()){
                                binding.txtAddDes.visibility = View.VISIBLE
                                binding.llDes.visibility = View.GONE
                            }else{
                                binding.txtAddDes.visibility = View.GONE
                                binding.llDes.visibility = View.VISIBLE
                                desGrp = it.data.description
                                binding.txtViewDes.text = it.data.description
                                binding.txtDesTime.text = UtilsDefault.dateConvert(UtilsDefault.localTimeConvert(it.data.description_updated_datetime))
                            }
                            binding.txtMediaCount.text = it.data.media_count.toString()
                            grpUserList = it.data.data
                            Glide.with(this).load(it.data.group_profile).placeholder(R.drawable.ic_default)
                                .error(R.drawable.ic_default).diskCacheStrategy(
                                    DiskCacheStrategy.DATA).into(binding.imgProfile)
                            binding.txtDesTime.text = UtilsDefault.monthName(UtilsDefault.localTimeConvert(it.data.created_datetime)!!)!!
                            if (it.data.data.size != 0) {
                                setData(it.data.data)
                                binding.txtParticipants.text =
                                    resources.getString(R.string.group) + " " + it.data.data.size + " " + resources.getString(
                                        R.string.participants)
                                binding.txtParticipants1.text =
                                    it.data.data.size.toString() + " " + resources.getString(R.string.participants)
                                for (i in 0 until it.data.data.size) {
                                    if (UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                                            .equals(it.data.data[i].user_id)
                                    ) {
                                        if (it.data.data[i].type.equals("admin")) {

                                            binding.llAddContact.visibility = View.VISIBLE
                                        } else {
                                            binding.llAddContact.visibility = View.GONE
                                        }
                                    }
                                }
                            }
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

    private fun setData(data: ArrayList<UserListGrp>) {
        list = data
//        for (a in contactList) {
//            for (b in 0 until list.size) {
//                if (PhoneNumberUtils.compare(a.Phn, list[b].phone)) {
//                    list.set(b,UserListGrp(list[b].user_id,
//                        a.name,
//                        list[b].type,
//                        list[b].about,
//                        list[b].phone,
//                        list[b].profile_pic))
//                }else{
//                    list.set(b,UserListGrp(list[b].user_id,
//                        list[b].phone,
//                        list[b].type,
//                        list[b].about,
//                        list[b].phone,
//                        list[b].profile_pic))
//                }
//            }
//        }
        if (list.size > 10) {
            binding.txtView.visibility = View.VISIBLE
            userListGrpAdapter!!.setUser(list.subList(0, 10))
        } else {
            binding.txtView.visibility = View.GONE
            userListGrpAdapter!!.setUser(list)
        }
    }

    private fun setOnClickListener() {
        binding.toolbar.imgBack.setOnClickListener {
            intent.putExtra("profile", profilePic)
            intent.putExtra("name", name)
            setResult(1, intent)
            finish()
        }

        binding.llMute.setOnClickListener {
            muteDialog()
        }

        binding.txtView.setOnClickListener {
            if (more){
                binding.txtView.text = resources.getString(R.string.show_more)
                more = false
                userListGrpAdapter!!.setUser(list)
            }else{
                binding.txtView.text = resources.getString(R.string.view_all)
                more = true
                if (list.size>10){
                    userListGrpAdapter!!.setUser(list.subList(0,10))
                }else{
                    userListGrpAdapter!!.setUser(list)
                }
            }
        }

        binding.txtEdit.setOnClickListener {
            showDialogChatInfo()
        }

        binding.llAddContact.setOnClickListener {
            startActivity(Intent(this, ExtraAddGroupActivity::class.java).putExtra("room", room))
        }

        binding.llExit.setOnClickListener {
            exitDialog()
        }

        binding.txtAddDes.setOnClickListener {
            showDialogChatDes()
        }

        binding.llDes.setOnClickListener {
            showDialogChatDes()
        }
    }

    private fun setMute(type: String) {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = room
        inputParams.type = type
        inputParams.show_notification = "0"

        apiViewModel.muteNotificaionGrp(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (it.data.end_time.equals("always")){
                                binding.txtMute.setText(it.data.end_time)
                            }else{
                                binding.txtMute.setText(resources.getString(R.string.until)+" "+UtilsDefault.dateMute(UtilsDefault.localTimeConvert(it.data.end_time)))
                            }
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

    private fun exitDialog() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_exit_grp, null)
            val dialogLogout = Dialog(this)
            val window: Window = dialogLogout.window!!
            window.setGravity(Gravity.CENTER)
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.FILL_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val bind = DialogExitGrpBinding.bind(view)
            dialogLogout.setCancelable(false)

            dialogLogout.setContentView(view)
            dialogLogout.show()

            bind.txtYes.setOnClickListener {
                dialogLogout.dismiss()
                val inputParams = InputParams()
                inputParams.accessToken =
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                inputParams.group_id = room
                exitGrp(inputParams)
            }

            bind.txtNo.setOnClickListener {
                dialogLogout.dismiss()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exitGrp(inputParams: InputParams) {
        apiViewModel.grpExit(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
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


    override fun onResume() {
        super.onResume()
        if (chatType.equals("private")) {
            binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
            binding.llGrpPart.visibility = View.GONE
        } else {
            binding.toolbar.txtHeader.text = resources.getString(R.string.group_info)
            binding.llGrpPart.visibility = View.VISIBLE
            getUserListGrp()
        }
    }

    @SuppressLint("Range")
    private fun getContactList() {
        try {
            val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
            while (phones!!.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                contactList!!.add(ContactListRes(name,phoneNumber))
                Log.d("name>>", name + "  " + phoneNumber)
            }
            phones.close()
        } catch (e: Exception) {
            Log.d("TAG", "getContactList: " + e)
        }
    }

    fun showDialogChatDes(){

        try {
            val view = layoutInflater.inflate(R.layout.dialog_bottom_des_grp, null)
            mBottomDialogDes = BottomSheetDialog(this)
            val bind = DialogBottomDesGrpBinding.bind(view)
            mBottomDialogDes!!.setCancelable(false)

            mBottomDialogDes!!.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            mBottomDialogDes!!.setContentView(view)
            mBottomDialogDes!!.show()

            if (!desGrp.isNullOrEmpty()){
                bind.edtGrpDes.setText(desGrp)
            }

            bind.txtCancel.setOnClickListener {
                bind.edtGrpDes.setText("")
                mBottomDialogDes!!.dismiss()
            }

            bind.txtDone.setOnClickListener {
                val des = bind.edtGrpDes.text.toString().trim()

                when {
                    TextUtils.isEmpty(des) -> toast(resources.getString(R.string.please_enter_grp_des))

                    else -> {
                        val inputParams = InputParams()
                        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                        inputParams.group_id = room
                        inputParams.description = des
                        changeGrpDes(inputParams)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeGrpDes(inputParams: InputParams) {
        apiViewModel.updateGrpDes(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            mBottomDialogDes!!.dismiss()
                            getUserListGrp()
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

    private fun showDialogChatInfo() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_bottom_chat_info_edit, null)
            mBottomDialogDocument = BottomSheetDialog(this)
            val bind = DialogBottomChatInfoEditBinding.bind(view)
            mBottomDialogDocument!!.setCancelable(true)

            imageView = view.findViewById(R.id.imgProfileDialog)

//            bottomSheetBehavior = BottomSheetBehavior.from((view.getParent() as View))
//            bottomSheetBehavior!!.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO)


            mBottomDialogDocument!!.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            mBottomDialogDocument!!.setContentView(view)
            mBottomDialogDocument!!.show()

            bind.edtGrpName.setText(name)

            bind.txtEdit.setOnClickListener {
                UtilsDefault.hideKeyboardForFocusedView(this)
                imagePermission {
                    imageSelectorDialog =
                        ImageSelectorDialog(this, this, resources.getString(R.string.profile_pht))
                }
            }

            Glide.with(this).applyDefaultRequestOptions(
                RequestOptions()
                    .error(R.drawable.ic_default)
            ).load(profilePic).diskCacheStrategy(
                DiskCacheStrategy.DATA).placeholder(R.drawable.ic_default)
                .into(imageView!!)

            bind.imgProfileDialog.setOnClickListener {
                UtilsDefault.hideKeyboardForFocusedView(this)
                imagePermission {
                    imageSelectorDialog =
                        ImageSelectorDialog(this, this, resources.getString(R.string.profile_pht))
                }
            }

            bind.imgClear.setOnClickListener {
                bind.edtGrpName.setText("")
            }

            bind.txtDone.setOnClickListener {
                UtilsDefault.hideKeyboardForFocusedView(this)

                val grpName = bind.edtGrpName.text.toString().trim()

                when {
                    TextUtils.isEmpty(grpName) -> toast(resources.getString(R.string.please_enter_grp_name))

                    else -> {
                        val inputParams = InputParams()
                        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                        inputParams.group_id = room
                        inputParams.group_name = grpName
                        changeGrpDetails(inputParams)
                    }
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeGrpDetails(inputParams: InputParams) {
        apiViewModel.changeGrpDetails(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                           toast(it.data.message)
                            mBottomDialogDocument!!.dismiss()
                            getUserListGrp()
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
                    pics = uri.path.toString()
                    val user_id: RequestBody =
                        UtilsDefault.getSharedPreferenceString(
                            Constants.USER_ID)!!
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    val accessToken: RequestBody =
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    val group_id: RequestBody =
                        room
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    val file = File(pics)
                    val requestBody =
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val profile = MultipartBody.Part.createFormData("group_profile",
                        file.name,
                        requestBody)
                    updateGrpProfile(user_id,accessToken,group_id,profile)

                    Glide.with(this).applyDefaultRequestOptions(
                        RequestOptions()
                            .error(R.drawable.ic_default)
                    ).load(pics).diskCacheStrategy(
                        DiskCacheStrategy.DATA).placeholder(R.drawable.ic_default)
                        .into(imageView!!)

                }
            }
            else -> {
                imageSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun updateGrpProfile(userId: RequestBody, accessToken: RequestBody, groupId: RequestBody, profile: MultipartBody.Part) {
        apiViewModel.changeGrpProfile(userId, accessToken,groupId, profile).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
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

    fun muteDialog(){
        try {
            val view = layoutInflater.inflate(R.layout.dialog_bottom_mute, null)
            mBottomDialogDocument = BottomSheetDialog(this)
            val bind = DialogBottomMuteBinding.bind(view)
            mBottomDialogDocument!!.setCancelable(false)

            mBottomDialogDocument!!.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            mBottomDialogDocument!!.setContentView(view)
            mBottomDialogDocument!!.show()

            bind.llCancel.setOnClickListener {
                mBottomDialogDocument!!.dismiss()
            }

            bind.llHrs.setOnClickListener {
                mBottomDialogDocument!!.dismiss()
                setMute("8_hours")
            }

            bind.llWeek.setOnClickListener {
                mBottomDialogDocument!!.dismiss()
                setMute("1_week")
            }

            bind.llWeek.setOnClickListener {
                mBottomDialogDocument!!.dismiss()
                setMute("always")
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}