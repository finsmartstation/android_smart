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
import android.os.Handler
import android.os.Looper
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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatBinding
import com.application.smartstation.databinding.DialogBottomChatDocumentBinding
import com.application.smartstation.databinding.MenuChatMorePopupBinding
import com.application.smartstation.databinding.MenuChatPopupBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.adapter.ChatHistoryAdapter
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.*
import com.application.smartstation.view.ImageVideoSelectorDialog
import com.application.smartstation.viewmodel.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devlomi.record_view.OnBasketAnimationEnd
import com.devlomi.record_view.OnRecordListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.vanniktech.emoji.EmojiImageView
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.emoji.Emoji
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import omrecorder.AudioChunk
import omrecorder.OmRecorder
import omrecorder.PullTransport
import omrecorder.Recorder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class ChatActivity : BaseActivity(), ImageVideoSelectorDialog.Action {

    val binding: ActivityChatBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var emojiPopup: EmojiPopup? = null
    var mBottomDialogDocument: BottomSheetDialog? = null
    var chatHistoryAdapter: ChatHistoryAdapter? = null
    var list: ArrayList<ChatDetailsRes> = ArrayList()
    val emitters: SocketService.Emitters = SocketService.Emitters(this)
    private var runTimePermission: RunTimePermission? = null
    var layoutManager: LinearLayoutManager? = null
    private var quotedMessagePos = -1
    var receiverId = ""
    var receiverName = ""
    var receiverProfile = ""
    var room = ""
    var timerStr = ""
    var chatType = ""
    var CAMERA_MIC_PERMISSION_REQUEST_CODE = 791
    var imageVideoSelectorDialog: ImageVideoSelectorDialog? = null
    var recorder: Recorder? = null
    var recordFile: File? = null
    val RECORD_START_AUDIO_LENGTH = 575


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

        binding.ilHeader.rlChat.setOnClickListener {
            if (!chatType.equals("private")) {
                startActivityForResult(Intent(this, ChatInfoActivity::class.java)
                    .putExtra(Constants.NAME, receiverName)
                    .putExtra(Constants.PROFILE_PIC, receiverProfile)
                    .putExtra(Constants.CHAT_TYPE, chatType).putExtra(Constants.ROOM, room),1)
            }
        }

        binding.imgEmoji.setOnClickListener {
            emojiPopup!!.toggle()
        }

        binding.ilHeader.imgMenu.setOnClickListener { v ->
            binding.ilHeader.imgMenu.visibility = View.INVISIBLE
            menuPopup(v)
        }

        binding.ilHeader.imgAudio.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                if (chatType.equals("private")) {
                    calling("voice_call")
                }
            }
        }

        binding.ilHeader.imgVideo.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                if (chatType.equals("private")) {
                    calling("video_call")
                }
            }
        }

        binding.imgPlus.setOnClickListener {
            binding.llMsg.visibility = View.GONE
            showDialogChat()
        }

        binding.llSend.setOnClickListener {
            val msg = binding.edtChat.text.toString().trim()
            when {
                TextUtils.isEmpty(msg) -> toast(resources.getString(R.string.please_msg))
                else -> {
                    sendMessage(msg, "text")
                }
            }
        }

        binding.ibtnCancel.setOnClickListener {
            binding.replyLayout.visibility = View.GONE
        }

    }

    private fun calling(type: String) {
        val intent = Intent(this@ChatActivity, CallActivity::class.java)
        intent.putExtra(Constants.REC_ID, receiverId)
        intent.putExtra(Constants.REC_NAME, receiverName)
        intent.putExtra(Constants.REC_PROFILE, receiverProfile)
        intent.putExtra(Constants.CALL_TYPE, type)
        intent.putExtra(Constants.STATUS, "Call_Send")
        intent.putExtra(Constants.ROOM_NAME, UtilsDefault.getRandomString(10))
        startActivity(intent)
    }

    private fun sendMessage(msg: String, type: String) {

        if (UtilsDefault.isOnline()) {
            if (chatType.equals("private")) {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                    jsonObject.put("accessToken",
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                    jsonObject.put("rid", receiverId)
                    jsonObject.put("type", type)
                    jsonObject.put("message", msg)
                    Log.d("TAG", "sendMessage: " + jsonObject)
                    emitters.sendMessage(jsonObject)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } else {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                    jsonObject.put("accessToken",
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                    jsonObject.put("room", room)
                    jsonObject.put("type", type)
                    jsonObject.put("message", msg)
                    Log.d("TAG", "sendMessage: " + jsonObject)
                    emitters.sendMessage(jsonObject)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            binding.edtChat.setText("")
//            layoutManager!!.scrollToPosition(list.size - 1)
        }
    }

    fun menuPopup(v: View?) {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.menu_chat_popup, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.RIGHT, 0, 0)

        val bind = MenuChatPopupBinding.bind(popupView)

        var i = 0

        bind.llMore.setOnClickListener {
            morePopup(v)
            i = 1
            popupWindow.dismiss()
        }

        bind.txtViewContact.setOnClickListener {
            if (!chatType.equals("private")) {
                startActivity(Intent(this, ChatInfoActivity::class.java)
                    .putExtra(Constants.NAME, receiverName)
                    .putExtra(Constants.PROFILE_PIC, receiverProfile)
                    .putExtra(Constants.CHAT_TYPE, chatType).putExtra(Constants.ROOM, room))
            }
            popupWindow.dismiss()
        }



        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
            if (i == 0) {
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
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.RIGHT, 0, 0)

        val bind = MenuChatMorePopupBinding.bind(popupView)

        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
            binding.ilHeader.imgMenu.visibility = View.VISIBLE
        })
    }

    private fun initView() {

        if (intent != null) {
            if (intent.getStringExtra(Constants.REC_ID) != null) {
                receiverId = intent.getStringExtra(Constants.REC_ID)!!
            }
            receiverName = intent.getStringExtra(Constants.NAME)!!
            receiverProfile = intent.getStringExtra(Constants.PROFILE)!!
            if (intent.getStringExtra(Constants.ROOM) != null) {
                room = intent.getStringExtra(Constants.ROOM)!!

            }
            if (intent.getStringExtra(Constants.CHAT_TYPE) != null) {
                chatType = intent.getStringExtra(Constants.CHAT_TYPE)!!
            } else {
                chatType = "private"
            }
            binding.ilHeader.txtName.text = intent.getStringExtra(Constants.NAME)
            Glide.with(this).load(intent.getStringExtra(Constants.PROFILE))
                .placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA)
                .into(binding.ilHeader.imgProfile)
        }

        if (chatType.equals("private")) {
            getChatDetails()
            roomEmit(receiverId)
            room = ""
        } else {
            getGrpChatDetails()
            grpRoomEmit(room)
        }

        runTimePermission = RunTimePermission(this)

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvChatRoom.layoutManager = layoutManager
        chatHistoryAdapter = ChatHistoryAdapter(this)
        binding.rvChatRoom.adapter = chatHistoryAdapter


        binding.rvChatRoom.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvChatRoom.postDelayed(Runnable {
                    try {
                        binding.rvChatRoom.smoothScrollToPosition(
                            binding.rvChatRoom.adapter!!.itemCount - 1
                        )
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }, 100)
            }
        })


//        chatHistoryAdapter!!.onItemClick = {
//            binding.rvChatRoom.smoothScrollToPosition(it - 1)
//            chatHistoryAdapter!!.blinkItem(it)
//        }

        chatHistoryAdapter!!.onItemClickImage = { pos ->
            if (!pos.message_type.equals("text")){
                UtilsDefault.downloadFile(this, pos.message, "Chat", object : MailCallback {
                    override fun success(resp: String?, status: Boolean?) {
                        if (status!!) {
                            if (resp!!.contains(".pdf")) {
                                startActivity(Intent(this@ChatActivity,
                                    PdfViewActivity::class.java).putExtra("path", resp))
                            } else {
                                FileUtils.openDocument(this@ChatActivity, resp)
                            }
                        }
                    }

                })
            }
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
                if (p0!!.length == 0) {
                    sendTypingIndicator(false)
                } else {
                    sendTypingIndicator(true)
                    typingHandler()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()
                var txt = p3
                if (text != "") {
                    binding.imgPlus.visibility = View.GONE
//                    binding.imgPay.visibility = View.GONE
//                    binding.imgCamera.visibility = View.GONE
                    binding.llSend.visibility = View.VISIBLE
                } else {
                    binding.imgPlus.visibility = View.VISIBLE
//                    binding.imgPay.visibility = View.VISIBLE
//                    binding.imgCamera.visibility = View.VISIBLE
                    binding.llSend.visibility = View.GONE
                }
            }

        })

        binding.edtChat.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                sendTypingIndicator(false)
            }
        }


        //record
        binding.recordView.cancelBounds = 0F
        binding.recordView.setSlideToCancelArrowColor(ContextCompat.getColor(this, R.color.grey))
        binding.recordView.setCounterTimeColor(ContextCompat.getColor(this, R.color.txt_color))
        binding.recordView.setSlideToCancelTextColor(ContextCompat.getColor(this, R.color.txt_color))
        binding.imgRec.setRecordView(binding.recordView)

        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                hideOrShowRecord(false)
                handleRecord()
            }

            override fun onCancel() {
                stopRecord(true, -1)
            }

            override fun onFinish(recordTime: Long) {
                hideOrShowRecord(true)
                stopRecord(false, recordTime)
//                requestEditTextFocus()
            }

            override fun onLessThanSecond() {
                Toast.makeText(this@ChatActivity,
                    R.string.voice_message_is_short_toast,
                    Toast.LENGTH_SHORT).show()
                hideOrShowRecord(true)
                stopRecord(true, -1)
//                requestEditTextFocus()
            }
        })

        binding.recordView.setOnBasketAnimationEndListener(OnBasketAnimationEnd {
            hideOrShowRecord(true)
//            requestEditTextFocus()
        })

    }

    private fun stopRecord(isCancelled: Boolean, recordTime: Long) {
        try {
            if (recorder != null) recorder!!.stopRecording()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        //if it's cancelled (the user swiped to cancel) then delete the recordFile
        if (isCancelled) {
            recordFile!!.delete()
        } else {
            //otherwise get the recordTime and convert it to Readable String and send the message
            timerStr = UtilsDefault.milliSecondsToTimer(recordTime)!!
            val filePath = recordFile!!.path
            Log.e("TAG", "stopRecord: "+filePath )
//            sendVoiceMessage(filePath, timerStr)
        }
    }

    private fun handleRecord() {
        recordFile = DirManager.generateFile(MessageType.SENT_VOICE_MESSAGE)
        recorder = OmRecorder.wav(
            PullTransport.Default(RecorderSettings.getMic(), object : PullTransport.OnAudioChunkPulledListener {
                override fun onAudioChunkPulled(audioChunk: AudioChunk?) {}
            }), recordFile)

        //start record when the record sound "BEEP" finishes
        Handler().postDelayed({ recorder!!.startRecording() },
            RECORD_START_AUDIO_LENGTH.toLong())
    }

    private fun hideOrShowRecord(hideRecord: Boolean) {
        if (hideRecord) {
            binding.recordView.setVisibility(View.INVISIBLE)
            binding.llTying.setVisibility(View.VISIBLE)
        } else {
            binding.recordView.setVisibility(View.VISIBLE)
            binding.llTying.setVisibility(View.GONE)
        }
    }

    private fun getGrpChatDetails() {
        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.group_id = room

        apiViewModel.getGrpDetails(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (it.data.data.user_left_status.equals("1")){
                                binding.linearLayout6.visibility = View.GONE
                                binding.llMsg.visibility = View.GONE
                            }else{
                                binding.linearLayout6.visibility = View.VISIBLE
                                binding.llMsg.visibility = View.VISIBLE
                            }
                            if (it.data.data.list.isNotEmpty()) {
                                setData(it.data.data.list, true)
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

    private fun getChatDetails() {
        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiverId

        apiViewModel.getChatDetailslist(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {

                            if (it.data.data.list.isNotEmpty()) {
                                setData(it.data.data.list, false)
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

    private fun setData(list: ArrayList<ChatDetailsRes>, chatType: Boolean) {
        this.list = list
        chatHistoryAdapter!!.setChatHis(list, chatType)
        layoutManager!!.scrollToPosition(chatHistoryAdapter!!.itemCount - 1)

        //reply msg
//        val messageSwipeController = MessageSwipeReplyView(this,list ,object : SwipeControllerActions {
//            override fun showReplyUI(position: Int) {
//                quotedMessagePos = position
//                showQuotedMessage(list[position])
//            }
//        })

//        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
//        itemTouchHelper.attachToRecyclerView(binding.rvChatRoom)
    }

    private fun roomEmit(receiverId: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("rid", receiverId)
                Log.d("TAG", "room: " + jsonObject)
                emitters.room(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun grpRoomEmit(receiverId: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("rid", receiverId)
                jsonObject.put("room", receiverId)
                Log.d("TAG", "room: " + jsonObject)
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
            if (dataResChat.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
                binding.txtReplyName.text = "You"
            } else {
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

            bind.llFolder.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                startActivity(Intent(this,CloudActivity::class.java))
                mBottomDialogDocument!!.dismiss()
            }

            bind.llCamera.setOnClickListener {
                runTimePermission!!.requestPermission(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), object : RunTimePermission.RunTimePermissionListener {
                    override fun permissionGranted() {
                        // First we need to check availability of play services
                        startActivityForResult(Intent(this@ChatActivity,
                            CameraActivity::class.java), RESULT_CODE)
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
                    imageVideoSelectorDialog = ImageVideoSelectorDialog(this@ChatActivity, this)
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

            bind.llFolder.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()

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
        Log.d("TAG", "ONCHATEVENT: " + jsonObject)
    }

    private fun setMessageEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val messageSocketModel: GetChatDetailsListResponse = gson.fromJson(jsonObject.toString(),
            GetChatDetailsListResponse::class.java)
        if (messageSocketModel.status) {
            if(!room.isNullOrEmpty()){
            if (messageSocketModel.data.id.equals(room)) {
                if (!messageSocketModel.data.list.isNullOrEmpty()) {
                    setData(messageSocketModel.data.list, true)
                }
            }
            }else{
//                if (messageSocketModel.data.id.equals(receiverId)) {
                    if (!messageSocketModel.data.list.isNullOrEmpty()) {
                        setData(messageSocketModel.data.list, false)
                    }
//                }
            }
        } else {
            toast(messageSocketModel.message)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOnlineChatEvent(event: OnlineChatEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setOnlineEvent(jsonObject)
        Log.d("TAG", "ONONLINEEVENT: " + jsonObject)
    }

    private fun setOnlineEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val onlineSocketModel: OnlineRes = gson.fromJson(jsonObject.toString(),
            OnlineRes::class.java)
        if (onlineSocketModel.online_status.equals("0")) {
            Log.d("TAG",
                "setOnlineEvent: " + UtilsDefault.dateLastSeen(onlineSocketModel.last_seen))
        } else {
            binding.ilHeader.txtStatus.text = "Online"
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

    fun checkPermissionForCameraAndMicrophone(): Boolean {
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
                val uri = Uri.fromParts("package", packageName, null)
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
            1 -> {
                if (data != null) {
                    if (!chatType.equals("private")){
                        binding.ilHeader.txtName.text = data.getStringExtra("name")
                        Glide.with(this).load(data.getStringExtra("profile"))
                            .placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                                DiskCacheStrategy.DATA)
                            .into(binding.ilHeader.imgProfile)
                    }
                }
            }
            else -> {
                imageVideoSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onImageSelected(imagePath: String, filename: String) {
        if (UtilsDefault.isImageFile(imagePath)) {
            val mIntent = Intent(this,
                ImagePerviewActivity::class.java)
            mIntent.putExtra(Constants.FILE_PATH, imagePath)
            mIntent.putExtra(Constants.TYPE, "img")
            startActivityForResult(mIntent, RESULT_CODE)
        } else {
            val mIntent = Intent(this,
                ImagePerviewActivity::class.java)
            mIntent.putExtra(Constants.FILE_PATH, imagePath)
            mIntent.putExtra(Constants.TYPE, "video")
            startActivityForResult(mIntent, RESULT_CODE)
        }
    }

    fun sendTypingIndicator(indicate: Boolean) {
        if (indicate) {
            if (chatType.equals("private")) {
                typingEmit("1")
            } else {
                typingGrpEmit("1")
            }
        } else {
            if (chatType.equals("private")) {
                typingEmit("0")
            } else {
                typingGrpEmit("0")
            }
        }
    }

    fun typingEmit(s: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("rid", receiverId)
                jsonObject.put("status", s)
                Log.d("TAG", "typing: " + jsonObject)
                emitters.typingStatus(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun typingGrpEmit(s: String) {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("room", receiverId)
                jsonObject.put("status", s)
                Log.d("TAG", "typing: " + jsonObject)
                emitters.typingGrpStatus(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
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
                    Log.d("TAG", "onFilePickerResult: " + uri)
                }
            }
            singleUri != null -> {
                val uri = singleUri
                val files = File(uri.toString())
                val requestBody =
                    files.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val file = MultipartBody.Part.createFormData("file", files.name, requestBody)
                val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val accessToken: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                    .toRequestBody("text/plain".toMediaTypeOrNull())
//                fileUpload(user_id,accessToken,file)
            }
            else -> return
        }
    }

    private fun fileUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        file: MultipartBody.Part,
    ) {
        apiViewModel.fileUpload(user_id, accessToken, file).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
//                            sendMessage(it.data.filepath, "image")
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

    fun typingHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            sendTypingIndicator(false)
        }, 2000)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingEvent(event: TypingEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setTypingEvent(jsonObject)
        Log.d("TAG", "TYPINGEVENT: " + jsonObject)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingEventGrp(event: TypingEventGrp) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setTypingEvent(jsonObject)
        Log.d("TAG", "TYPINGEVENT: " + jsonObject)
    }

    fun setTypingEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val typingModel: TypingRes = gson.fromJson(jsonObject.toString(),
            TypingRes::class.java)
        if (typingModel.status) {
            if (chatType.equals("private")) {
                if (typingModel.user_id.equals(receiverId)) {
                    if (typingModel.typing.equals("1")) {
                        binding.ilHeader.txtStatus.visibility = View.GONE
                        binding.ilHeader.txtTypingStatus.visibility = View.VISIBLE
                    } else {
                        binding.ilHeader.txtStatus.visibility = View.VISIBLE
                        binding.ilHeader.txtTypingStatus.visibility = View.GONE
                    }
                }
            } else {
                if (!typingModel.user_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
                    if (typingModel.typing.equals("1")) {
                        binding.ilHeader.txtStatus.visibility = View.GONE
                        binding.ilHeader.txtTypingStatus.visibility = View.VISIBLE
                        binding.ilHeader.txtTypingStatus.text =
                            typingModel.name + " " + resources.getString(R.string.typing)
                    } else {
                        binding.ilHeader.txtStatus.visibility = View.VISIBLE
                        binding.ilHeader.txtTypingStatus.visibility = View.GONE
                    }
                }
            }
        }
    }

}