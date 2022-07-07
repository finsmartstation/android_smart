package com.application.smartstation.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatBinding
import com.application.smartstation.databinding.DialogBottomChatDocumentBinding
import com.application.smartstation.databinding.MenuChatMorePopupBinding
import com.application.smartstation.databinding.MenuChatPopupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.adapter.ChatHistoryAdapter
import com.application.smartstation.ui.model.ChatDetailsRes
import com.application.smartstation.ui.model.DataResChat
import com.application.smartstation.ui.model.GetChatDetailsListResponse
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.*
import com.application.smartstation.view.ImageVideoSelectorDialog
import com.application.smartstation.view.MessageSwipeReplyView
import com.application.smartstation.viewmodel.ApiViewModel
import com.application.smartstation.viewmodel.ChatEvent
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.gson.Gson
import com.vanniktech.emoji.EmojiImageView
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.emoji.Emoji
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class ChatActivity : BaseActivity(),ImageVideoSelectorDialog.Action {

    val binding:ActivityChatBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var emojiPopup:EmojiPopup? = null
    var mBottomDialogDocument: BottomSheetDialog? = null
    var chatHistoryAdapter: ChatHistoryAdapter? = null
    var list:ArrayList<ChatDetailsRes> = ArrayList()
    val emitters: SocketService.Emitters = SocketService.Emitters(this)
    private var runTimePermission: RunTimePermission? = null
    var layoutManager:LinearLayoutManager? = null
    private var quotedMessagePos = -1
    var receiverId = ""
    var receiverName = ""
    var receiverProfile = ""
    var CAMERA_MIC_PERMISSION_REQUEST_CODE = 791
    var imageVideoSelectorDialog: ImageVideoSelectorDialog? = null
    var sendTypingIndication: DatabaseReference? = null
    var receiveTypingIndication: DatabaseReference? = null

    val mimeTypes = arrayOf(
        "image/jpeg", // jpeg or jpg
        "image/png", // png
        "application/pdf", // pdf
        "application/msword", // doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
        "application/vnd.ms-excel", // xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
        "video/mp4", // mp4
        "audio/mpeg", // mp3
    )

    companion object {
        val RESULT_CODE = 124
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryStatusBar()
        setContentView(R.layout.activity_chat)
        initView()
        setOnClickListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.imgEmoji.setOnClickListener {
            emojiPopup!!.toggle()
        }

        binding.ilHeader.imgMenu.setOnClickListener {v ->
            binding.ilHeader.imgMenu.visibility = View.INVISIBLE
            menuPopup(v)
        }

        binding.ilHeader.imgAudio.setOnClickListener {
           if (!checkPermissionForCameraAndMicrophone()) {
               requestPermissionForCameraAndMicrophone()
           }else{
               calling("voice_call")
           }
        }

        binding.ilHeader.imgVideo.setOnClickListener {
           if (!checkPermissionForCameraAndMicrophone()) {
               requestPermissionForCameraAndMicrophone()
           }else{
               calling("video_call")
           }
        }

//        binding.imgCamera.setOnClickListener {

//
//        }

        binding.imgPlus.setOnClickListener {
            binding.llMsg.visibility = View.GONE
            showDialogChat()
        }

        binding.llSend.setOnClickListener{
            val msg = binding.edtChat.text.toString().trim()
            when {
                TextUtils.isEmpty(msg) -> toast(resources.getString(R.string.please_msg))
                else -> {
                    sendMessage(msg,"text")
                }
            }
        }
        
        binding.imgRec.setOnClickListener {

        }

        binding.ibtnCancel.setOnClickListener {
            binding.replyLayout.visibility = View.GONE
        }

    }

    private fun calling(type:String) {
        val intent = Intent(this@ChatActivity,CallActivity::class.java)
        intent.putExtra(Constants.REC_ID,receiverId)
        intent.putExtra(Constants.REC_NAME,receiverName)
        intent.putExtra(Constants.REC_PROFILE,receiverProfile)
        intent.putExtra(Constants.CALL_TYPE,type)
        intent.putExtra(Constants.STATUS,"Call_Send")
        intent.putExtra(Constants.ROOM_NAME,UtilsDefault.getRandomString(10))
        startActivity(intent)
    }

    private fun sendMessage(msg: String, type: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("rid", receiverId)
                jsonObject.put("type", type)
                jsonObject.put("message", msg)
                Log.d("TAG", "sendMessage: "+jsonObject)
                emitters.sendMessage(jsonObject)
                updateMessages(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            binding.edtChat.setText("")
//            layoutManager!!.scrollToPosition(list.size - 1)
        }
    }

    private fun updateMessages(jsonObject: JSONObject) {

    }

    fun menuPopup(v: View?) {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.menu_chat_popup, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setFocusable(true)
        popupWindow.setOutsideTouchable(true)
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.RIGHT, 0, 0)

        val bind = MenuChatPopupBinding.bind(popupView)

        var i = 0

        bind.llMore.setOnClickListener {
            morePopup(v)
            i = 1
            popupWindow.dismiss()
        }

        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
            if (i == 0){
                binding.ilHeader.imgMenu.visibility = View.VISIBLE
            }
        })
    }

    fun morePopup(v: View?) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.menu_chat_more_popup, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setFocusable(true)
        popupWindow.setOutsideTouchable(true)
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.RIGHT, 0, 0)

        val bind = MenuChatMorePopupBinding.bind(popupView)

        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
            binding.ilHeader.imgMenu.visibility = View.VISIBLE
        })
    }

    private fun initView() {

        if (intent != null) {
            receiverId = intent.getStringExtra(Constants.REC_ID)!!
            receiverName = intent.getStringExtra(Constants.NAME)!!
            receiverProfile = intent.getStringExtra(Constants.PROFILE)!!
            binding.ilHeader.txtName.text = intent.getStringExtra(Constants.NAME)
            Glide.with(this).load(intent.getStringExtra(Constants.PROFILE))
                .into(binding.ilHeader.imgProfile)
        }

        getChatDetails()

        receiveTypeIndication()

        roomEmit(receiverId)

        runTimePermission = RunTimePermission(this)

        layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        binding.rvChatRoom.layoutManager = layoutManager
        chatHistoryAdapter = ChatHistoryAdapter(this)
        binding.rvChatRoom.adapter = chatHistoryAdapter


        binding.rvChatRoom.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvChatRoom.postDelayed(Runnable {
                    try {
                        binding.rvChatRoom.smoothScrollToPosition(
                            binding.rvChatRoom.getAdapter()!!.getItemCount() - 1
                        )
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }, 100)
            }
        })

        //reply msg
        val messageSwipeController = MessageSwipeReplyView(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                quotedMessagePos = position
                showQuotedMessage(list[position])
            }
        })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.rvChatRoom)

        chatHistoryAdapter!!.onItemClick = {
            binding.rvChatRoom.smoothScrollToPosition(it - 1)
            chatHistoryAdapter!!.blinkItem(it)
        }

        emojiPopup = EmojiPopup.Builder.fromRootView(binding.llChat)
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
            .build(binding.edtChat)

        //chat msg listener
        binding.edtChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                if (text!=""){
                    binding.imgPlus.visibility = View.GONE
//                    binding.imgPay.visibility = View.GONE
//                    binding.imgCamera.visibility = View.GONE
                    binding.llSend.visibility = View.VISIBLE
                }
                else {
                    binding.imgPlus.visibility = View.VISIBLE
//                    binding.imgPay.visibility = View.VISIBLE
//                    binding.imgCamera.visibility = View.VISIBLE
                    binding.llSend.visibility = View.GONE
                }

                if (p3 == 0) {
                    sendTypingIndicator(false)
                } else {
                    sendTypingIndicator(true)
                }
            }

        })

        binding.edtChat.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                sendTypingIndicator(false)
            }
        })

    }

    private fun getChatDetails() {
        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiverId

        apiViewModel.getChatDetailslist(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                           if (it.data.data.list.isNotEmpty()){
                               setData(it.data.data.list)
                           }
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

    private fun setData(list: ArrayList<ChatDetailsRes>) {
        chatHistoryAdapter!!.setChatHis(list)
        layoutManager!!.scrollToPosition(chatHistoryAdapter!!.getItemCount() - 1)
    }

    private fun roomEmit(receiverId: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("rid", receiverId)
                Log.d("TAG", "room: "+jsonObject)
                emitters.room(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    //reply msg
    private fun showQuotedMessage(dataResChat: ChatDetailsRes) {
        binding.replyLayout.visibility = View.VISIBLE
        binding.edtChat.requestFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.edtChat, InputMethodManager.SHOW_IMPLICIT)

        if (dataResChat.message_type.equals("text")) {
            binding.textQuotedMessage.text = dataResChat.message
            if (dataResChat.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))){
                binding.txtReplyName.text = "You"
            }else{
                binding.txtReplyName.text = receiverName
            }

        }
    }

    private fun showDialogChat() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_bottom_chat_document, null)
            mBottomDialogDocument = BottomSheetDialog(this)
            val bind = DialogBottomChatDocumentBinding.bind(view)
            mBottomDialogDocument!!.setCancelable(false)

            mBottomDialogDocument!!.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            mBottomDialogDocument!!.setContentView(view)
            mBottomDialogDocument!!.show()

            bind.llCancel.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
            }

            bind.llCamera.setOnClickListener {
                runTimePermission!!.requestPermission(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), object : RunTimePermission.RunTimePermissionListener {
                    override fun permissionGranted() {
                        // First we need to check availability of play services
                        startActivityForResult(Intent(this@ChatActivity,CameraActivity::class.java),RESULT_CODE)
                    }

                    override fun permissionDenied() {
                    }
                })
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
            }

            bind.llPhoto.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                imagePermission {
                    imageVideoSelectorDialog = ImageVideoSelectorDialog(this@ChatActivity,this)
                }
                mBottomDialogDocument!!.dismiss()
            }

            bind.llDocument.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
                imagePermission {
                    startFilePicker()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvent(event: ChatEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setMessageEvent(jsonObject)
        Log.d("TAG", "ONCHATEVENT: "+jsonObject)
    }

    private fun setMessageEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val messageSocketModel: GetChatDetailsListResponse = gson.fromJson(jsonObject.toString(),
            GetChatDetailsListResponse::class.java)
        if (messageSocketModel.status){
            if (messageSocketModel.data.list.isNotEmpty()){
                setData(messageSocketModel.data.list)
            }
        }else{
            toast(messageSocketModel.message)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun checkPermissionForCameraAndMicrophone(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val resultCamera =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val resultMic =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            resultCamera == PackageManager.PERMISSION_GRANTED &&
                    resultMic == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestPermissionForCameraAndMicrophone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(
                    Manifest.permission.RECORD_AUDIO)
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", getPackageName(), null)
                intent.data = uri
                startActivity(intent)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            124 -> {
                if (data != null) {
                    val value = data.getStringExtra("file_url")
                    sendMessage(value!!, "image")
                }
            }
            else -> {
                imageVideoSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onImageSelected(imagePath: String, filename: String) {
        if (UtilsDefault.isImageFile(imagePath)){
            val mIntent = Intent(this,
                ImagePerviewActivity::class.java)
            mIntent.putExtra(Constants.FILE_PATH, imagePath)
            mIntent.putExtra(Constants.TYPE, "img")
            startActivityForResult(mIntent, RESULT_CODE)
        }else{
            val mIntent = Intent(this,
                ImagePerviewActivity::class.java)
            mIntent.putExtra(Constants.FILE_PATH, imagePath)
            mIntent.putExtra(Constants.TYPE, "video")
            startActivityForResult(mIntent, RESULT_CODE)
        }
    }

    fun sendTypingIndicator(indicate: Boolean) {
        // if the type indicator is present then we remove it if not then we create the typing indicator
        if (indicate) {
            val message_user_map = HashMap<Any, Any>()
            message_user_map["receiver_id"] = receiverId
            message_user_map["sender_id"] = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
            sendTypingIndication =
                FirebaseDatabase.getInstance().reference.child("typing_indicator")
            sendTypingIndication!!.child(UtilsDefault.getSharedPreferenceString(Constants.USER_ID) + "-" + receiverId).setValue(message_user_map)
                .addOnSuccessListener(
                    OnSuccessListener<Void?> {
                        sendTypingIndication!!.child(receiverId + "-" + UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                            .setValue(message_user_map).addOnSuccessListener(
                                OnSuccessListener<Void?> { })
                    })
        } else {
            sendTypingIndication =
                FirebaseDatabase.getInstance().reference.child("typing_indicator")
            sendTypingIndication!!.child(UtilsDefault.getSharedPreferenceString(Constants.USER_ID) + "-" + receiverId).removeValue()
                .addOnCompleteListener(
                    OnCompleteListener<Void?> {
                        sendTypingIndication!!.child(UtilsDefault.getSharedPreferenceString(Constants.USER_ID) + "-" + receiverId).removeValue()
                            .addOnCompleteListener(
                                OnCompleteListener<Void?> { })
                    })
        }
    }

    fun receiveTypeIndication() {
        receiveTypingIndication = FirebaseDatabase.getInstance().reference.child("typing_indicator")
        receiveTypingIndication!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(receiverId + "-" + UtilsDefault.getSharedPreferenceString(Constants.USER_ID)).exists()) {
                    val receiver = dataSnapshot.child(receiverId + "-" + UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                        .child("sender_id").value.toString()
                    if (receiver == receiverId) {
                        binding.typeindicator.setVisibility(View.VISIBLE)
                    }
                } else {
                    binding.typeindicator.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        sendTypingIndicator(false)
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::onFilePickerResult
    )

    private fun startFilePicker() {
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickerIntent.type = "*/*"
        pickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        filePickerLauncher.launch(pickerIntent)
    }

    private fun onFilePickerResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        val multipleUriData = result.data?.clipData
        val singleUri = result.data?.data

        when {
            multipleUriData != null -> {
                for (i in 0 until multipleUriData.itemCount) {
                    val uri = multipleUriData.getItemAt(i).uri
                    Log.d("TAG", "onFilePickerResult: "+uri)
                }
            }
            singleUri != null -> {
                val uri = singleUri
                val files = File(uri.toString())
                val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), files)
                val file = MultipartBody.Part.createFormData("file", files.getName(), requestBody)
                val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!)
                val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                )
//                fileUpload(user_id,accessToken,file)
            }
            else -> return
        }
    }

    private fun fileUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        file: MultipartBody.Part
    ) {
        apiViewModel.fileUpload(user_id, accessToken, file).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            sendMessage(it.data.filepath, "image")
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