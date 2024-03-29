package com.application.smartstation.ui.activity

import android.Manifest
import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.listener.OnStickerActions
import com.aghajari.emojiview.listener.PopupListener
import com.aghajari.emojiview.search.AXEmojiSearchView
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.view.AXEmojiPager
import com.aghajari.emojiview.view.AXEmojiPopup
import com.aghajari.emojiview.view.AXEmojiView
import com.aghajari.emojiview.view.AXStickerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatViewBinding
import com.application.smartstation.databinding.DialogBottomChatDocumentBinding
import com.application.smartstation.databinding.MenuChatMorePopupBinding
import com.application.smartstation.databinding.MenuChatPopupBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.activity.ChatViewGrpActivity.Companion.RESULT_CODE
import com.application.smartstation.ui.adapter.AdapterChat
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.*
import com.application.smartstation.util.FileUtils
import com.application.smartstation.view.EmojiEditText.KeyBoardInputCallbackListener
import com.application.smartstation.view.ImageVideoSelectorDialog
import com.application.smartstation.view.sticker.FireStickerProvider
import com.application.smartstation.view.sticker.StickerLoader
import com.application.smartstation.viewmodel.ApiViewModel
import com.application.smartstation.viewmodel.ChatEvent
import com.application.smartstation.viewmodel.OnlineChatEvent
import com.application.smartstation.viewmodel.TypingEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.devlomi.record_view.OnBasketAnimationEnd
import com.devlomi.record_view.OnRecordListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.smartstation.simpleplacepicker.MapActivity
import com.smartstation.simpleplacepicker.utils.SimplePlacePicker
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup
import com.wafflecopter.multicontactpicker.ContactResult
import com.wafflecopter.multicontactpicker.MultiContactPicker
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
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
class ChatViewActivity : BaseActivity(), ImageVideoSelectorDialog.Action {

    val binding: ActivityChatViewBinding by viewBinding()
    var imageVideoSelectorDialog: ImageVideoSelectorDialog? = null
    var chatType = ""
    val apiViewModel: ApiViewModel by viewModels()
    val emitters: SocketService.Emitters = SocketService.Emitters(this)
    var room = ""
    var receiverId = ""
    var type = ""
    var receiverName = ""
    var receiverProfile = ""
    var searchIndex = 0
    var imageParts: ArrayList<MultipartBody.Part?> = ArrayList()
    var list: ArrayList<ChatDetailsRes> = ArrayList()
    var chatHistoryAdapter: AdapterChat? = null
    var layoutManager: LinearLayoutManager? = null
    var popupWindow:PopupWindow? = null

    //request
    val PICK_CONTACT_REQUEST: Int = 5491
    val PICK_NUMBERS_FOR_CONTACT_REQUEST = 5517
    val PICK_LOCATION_REQUEST = 7125
    var CAMERA_MIC_PERMISSION_REQUEST_CODE = 791

    //emoji
    var emojiPopup: AXEmojiPopup? = null

    //record
    var recorder: Recorder? = null
    var recordFile: File? = null
    val RECORD_START_AUDIO_LENGTH = 575
    var timerStr = ""

    //bottom dialog
    var mBottomDialogDocument: BottomSheetDialog? = null

    //permission
    private var runTimePermission: RunTimePermission? = null

    //random numbers just to identify requestCode
    val PICK_MUSIC_REQUEST = 159

    var blkStatus = false


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_view)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        StickerLoader(this).loadStickersIntoFilesDir()

        initEmojiView()

        if (intent != null) {
            if (intent.getStringExtra(Constants.REC_ID) != null) {
                receiverId = intent.getStringExtra(Constants.REC_ID)!!
            }
            receiverName = intent.getStringExtra(Constants.NAME)!!
            receiverProfile = intent.getStringExtra(Constants.PROFILE)!!
            if (intent.getStringExtra(Constants.ROOM) != null) {
                room = intent.getStringExtra(Constants.ROOM)!!

            }
            room = ""
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

        binding.rvChatRoom.setHasFixedSize(true)
        binding.rvChatRoom.setItemViewCacheSize(20)
        binding.rvChatRoom.setDrawingCacheEnabled(true)
        binding.rvChatRoom.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)

        runTimePermission = RunTimePermission(this)

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvChatRoom.layoutManager = layoutManager
        chatHistoryAdapter = AdapterChat(this)
        binding.rvChatRoom.adapter = chatHistoryAdapter

        chatHistoryAdapter!!.onItemClick = {it ->
            if (Constants.IMAGE.equals(it.message_type)){
                if (it.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
                    val intent = Intent(this,MediaViewActivity::class.java)
                    intent.putExtra("name","You")
                    intent.putExtra("date",it.date)
                    intent.putExtra("msg",it.message)
                    intent.putExtra("type",it.message_type)
                    startActivity(intent)
                } else {
                    UtilsDefault.downloadFile(this, it.message, "image", object : MailCallback {
                        override fun success(resp: String?, status: Boolean?) {
                            if (status!!) {
                                val intent =
                                    Intent(this@ChatViewActivity, MediaViewActivity::class.java)
                                intent.putExtra("name", receiverName)
                                intent.putExtra("date", it.date)
                                intent.putExtra("msg", it.message)
                                intent.putExtra("type", it.message_type)
                                startActivity(intent)
                            }
                        }
                    })
                }

            }else if (Constants.VIDEO.equals(it.message_type)){
                UtilsDefault.downloadFile(this, it.message, "video", object : MailCallback {
                    override fun success(resp: String?, status: Boolean?) {
                        if (status!!) {
                            if (it.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
                                val intent = Intent(this@ChatViewActivity,MediaViewActivity::class.java)
                                intent.putExtra("name","You")
                                intent.putExtra("date",it.date)
                                intent.putExtra("msg",resp)
                                intent.putExtra("type",it.message_type)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@ChatViewActivity,MediaViewActivity::class.java)
                                intent.putExtra("name",receiverName)
                                intent.putExtra("date",it.date)
                                intent.putExtra("msg",resp)
                                intent.putExtra("type",it.message_type)
                                startActivity(intent)
                            }
                        }
                    }

                })

            }else if (Constants.AUDIO.equals(it.message_type)){
//                if (it.senter_id.equals(UtilsDefault.getSharedPreferenceString(Constants.USER_ID))) {
//                    val intent = Intent(this@ChatViewActivity,MediaViewActivity::class.java)
//                    intent.putExtra("name","You")
//                    intent.putExtra("date",it.date)
//                    intent.putExtra("msg",it.message)
//                    intent.putExtra("type",it.message_type)
//                    startActivity(intent)
//                } else {
//                    UtilsDefault.downloadFile(this, it.message, "image", object : MailCallback {
//                        override fun success(resp: String?, status: Boolean?) {
//                            if (status!!) {
//                                val intent = Intent(this@ChatViewActivity, MediaViewActivity::class.java)
//                                intent.putExtra("name", receiverName)
//                                intent.putExtra("date", it.date)
//                                intent.putExtra("msg", it.message)
//                                intent.putExtra("type", it.message_type)
//                                startActivity(intent)
//                            }
//                        }
//                    })
//                }
            }
        }


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

        roomEmit(receiverId)
        getChatDetails()

        //edit text
        //onSendButton Click in keyboard
        binding.edtChat.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage(binding.edtChat.getText().toString())
                return@OnEditorActionListener true
            }
            false
        })

        binding.edtChat.setKeyBoardInputCallbackListener(object : KeyBoardInputCallbackListener {
            override fun onCommitContent(
                inputContentInfo: InputContentInfoCompat?,
                flags: Int, opts: Bundle?,
            ) {
                val contentUri: Uri = inputContentInfo!!.getContentUri()
                try {
                    if (BuildVerUtil.isApi29OrAbove()) {
                        sendImage(contentUri.toString(), Constants.STICKERS)
                    } else {
                        val file = File.createTempFile("temp", ".gif")
                        val b: Boolean =
                            FileUtils.writeToFileFromContentUri(contentResolver, file, contentUri)
                        if (b) {
                            sendImage(file.path, Constants.STICKERS)
                        } else {
//                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT)
//                            .show()
                        }
                    }
                } catch (e: java.lang.Exception) {
//                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        })

        binding.edtChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val text = s.toString()
                if (s!!.length == 0) {
                    sendTypingIndicator(false)
                } else {
                    sendTypingIndicator(true)
                    typingHandler()
                }
                if (text != "") {
                    binding.imgPlus.visibility = View.GONE
                    binding.llSend.visibility = View.VISIBLE
                } else {
                    binding.imgPlus.visibility = View.VISIBLE
                    binding.llSend.visibility = View.GONE
                }
            }
        })

        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone()
        }

        //searchview

        binding.tbSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                val listSearch = searchTxt(query)

                if (!listSearch.isEmpty()) {

                    //get the found last message index
                    searchIndex = listSearch.size - 1
                    val foundMessageId: String = listSearch[searchIndex].id
                    val mIndex: Int = getPosFromId(foundMessageId)
                    scrollAndHighlightSearch(mIndex)
                    binding.tbDownArrow.setOnClickListener(View.OnClickListener { //+2 because one for index and one for previous
                        //check if there are another results
                        if (listSearch.isEmpty() || searchIndex + 2 > listSearch.size) {
//                            toast(resources.getString(R.string.not_found))
                            return@OnClickListener
                        }

                        //increment current index
                        searchIndex++
                        val foundMessageId: String = listSearch[searchIndex].id
                        //get the index from chatList by message id from searchedList
                        val mIndex: Int = getPosFromId(foundMessageId)
                        scrollAndHighlightSearch(mIndex)
                    })
                    binding.tbUpArrow.setOnClickListener(View.OnClickListener {
                        if (listSearch.isEmpty() || searchIndex - 1 < 0) {
//                            toast(resources.getString(R.string.not_found))
                            return@OnClickListener
                        }


                        //decrement search index
                        searchIndex -= 1
                        val foundMessageId: String = listSearch[searchIndex].id
                        val mIndex: Int = getPosFromId(foundMessageId)
                        scrollAndHighlightSearch(mIndex)
                    })
                }else{
                    setData(list)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        binding.tbSearchView.setOnQueryTextFocusChangeListener(View.OnFocusChangeListener { view, b ->
            if (b)
                UtilsDefault.showKeyboardForFocusedView(this)
        })

        binding.tbSearchView.setOnCloseListener(SearchView.OnCloseListener {
//            isInSearchMode = false
            true
        })


    }

    fun searchTxt(txt:String):ArrayList<ChatDetailsRes>{
        val templist: java.util.ArrayList<ChatDetailsRes> = java.util.ArrayList()
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            for (items in list) {
                val chat = items.message.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            return templist
        }else{
            setData(list)
           templist.clear()
            return templist
        }
    }

    private fun scrollAndHighlightSearch(mIndex: Int) {
        binding.rvChatRoom.scrollToPosition(mIndex)
        val view = this.currentFocus
        if(view != null)
        UtilsDefault.hideKeyboardForFocusedView(this)
        Handler().postDelayed({ //get view holder of this textView
            val viewHolderForAdapterPosition: RecyclerView.ViewHolder =
                binding.rvChatRoom.findViewHolderForAdapterPosition(mIndex)!!
            //get textView
            val tv =
                viewHolderForAdapterPosition.itemView.findViewById<TextView>(R.id.txtMsg)
            //highlight text
            tv.setText(UtilsDefault.highlightText(tv.text.toString()))
            tv.setTextColor(resources.getColor(R.color.black))
        }, 100)
    }


    //get index from list using the id
    private fun getPosFromId(messageId: String): Int {
        for (a in 0 until list.size){
            if (list[a].id.equals(messageId)){
                return a
            }
        }
        return 0
    }

    fun typingHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            sendTypingIndicator(false)
        }, 2000)
    }

    fun statusBlk(status:Boolean):Boolean{
        if (status){
            toast(resources.getString(R.string.please_unblk))
            return true
        }
        return false
    }

    private fun sendImage(img: String, docs: String) {
        if (img.trim { it <= ' ' }.isEmpty()) return
        if (statusBlk(blkStatus)) return
        emojiPopup!!.dismiss()

        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("accessToken",
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                jsonObject.put("rid", receiverId)
                jsonObject.put("type",docs)
                jsonObject.put("message", img)
                Log.d("TAG", "sendMessage: " + jsonObject)
                emitters.sendMessage(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
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
                            if (it.data.data.user_block_status.equals(0)){
                                blkStatus = false
                            }else{
                                blkStatus = true
                            }
                            if (it.data.data.list.isNotEmpty()) {
                                setData(it.data.data.list)
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

    //send text message
    private fun sendMessage(txt: String) {
        if (txt.trim { it <= ' ' }.isEmpty()) return
        if (statusBlk(blkStatus)) return
        emojiPopup!!.dismiss()

        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("sid", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("accessToken",
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                jsonObject.put("rid", receiverId)
                jsonObject.put("type", Constants.TEXT)
                jsonObject.put("message", txt)
                Log.d("TAG", "sendMessage: " + jsonObject)
                emitters.sendMessage(jsonObject)
                binding.edtChat.setText("")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initEmojiView() {
        val emojiPager = AXEmojiPager(this)
        val emojiView = AXEmojiView(this)
        emojiPager.addPage(emojiView, R.drawable.ic_smile_message_icon_use)
        // set target emoji edit text to emojiViewPager
        emojiPager.editText = binding.edtChat

        emojiPager.setSwipeWithFingerEnabled(true)
        emojiPager.setLeftIcon(R.drawable.ic_search_use)

        val stickerView = AXStickerView(this, "stickers", FireStickerProvider(this))
        emojiPager.addPage(stickerView, R.drawable.ic_masks_msk)

//        add sticker click listener
        stickerView.setOnStickerActionsListener(object : OnStickerActions {
            override fun onClick(view: View, sticker: Sticker<*>, fromRecent: Boolean) {
                try {
                    val path = sticker.data as String
                    if (File(path).exists()) {
//                        sendSticker(path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onLongClick(
                view: View,
                sticker: Sticker<*>?,
                fromRecent: Boolean,
            ): Boolean {
                return false
            }
        })


        emojiPopup = AXEmojiPopup(emojiPager)


        emojiPopup!!.setPopupListener(object : PopupListener {
            override fun onDismiss() {
                binding.imgEmoji.setImageResource(R.drawable.ic_smile_message_icon_use)
            }

            override fun onShow() {
                binding.imgEmoji.setImageResource(R.drawable.ic_keyboard)
            }

            override fun onKeyboardOpened(height: Int) {}
            override fun onKeyboardClosed() {}
            override fun onViewHeightChanged(height: Int) {}
        })

        // SearchView
        if (AXEmojiManager.isAXEmojiView(emojiPager.getPage(0))) {
            emojiPopup!!.setSearchView(AXEmojiSearchView(this, emojiPager.getPage(0)))
            emojiPager.setOnFooterItemClicked(object: AXEmojiPager.OnFooterItemClicked{
                override fun onClick(view: View?, leftIcon: Boolean) {
                    emojiPopup!!.showSearchView()
                }
            })
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
                toast(R.string.voice_message_is_short_toast.toString())
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
            val files = File(filePath)
            val requestBody =
                files.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val file = MultipartBody.Part.createFormData("file", files.name, requestBody)
            val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val accessToken: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                .toRequestBody("text/plain".toMediaTypeOrNull())
            fileUpload(user_id, accessToken, file,Constants.AUDIO)
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
            binding.llRecView.setVisibility(View.GONE)
            binding.llMsg.setVisibility(View.VISIBLE)
        } else {
            binding.llRecView.setVisibility(View.VISIBLE)
            binding.llMsg.setVisibility(View.GONE)
        }
    }


    private fun setOnClickListener() {

        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.edtChat.setOnClickListener{
            emojiPopup!!.dismiss()
        }

        binding.imgEmoji.setOnClickListener{ emojiPopup!!.toggle() }

        binding.imgPlus.setOnClickListener {
            showDialogChat()
        }

        binding.llSend.setOnClickListener {
            val msg = binding.edtChat.text.toString().trim()
            when {
                TextUtils.isEmpty(msg) -> toast(resources.getString(R.string.please_msg))
                else -> {
                    sendMessage(msg)
                }
            }
        }

        binding.ilHeader.rlChat.setOnClickListener {
            startActivity(Intent(this, PrivateChatInfoActivity::class.java)
                .putExtra(Constants.REC_ID, receiverId))
        }


        binding.ilHeader.imgMenu.setOnClickListener { v ->
            binding.ilHeader.imgMenu.visibility = View.INVISIBLE
            menuPopup(v)
        }

        binding.ilHeader.imgAudio.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                calling("voice_call")
            }
        }

        binding.ilHeader.imgVideo.setOnClickListener {
            if (!checkPermissionForCameraAndMicrophone()) {
                requestPermissionForCameraAndMicrophone()
            } else {
                calling("video_call")
            }
        }

        binding.imgBack1.setOnClickListener {
            binding.llSearchView.visibility = View.GONE
            binding.ilHeader.rlChat.visibility = View.VISIBLE
        }
    }

    private fun calling(type: String) {
        if (statusBlk(blkStatus)) return
        val intent = Intent(this@ChatViewActivity, CallActivity::class.java)
        intent.putExtra(Constants.REC_ID, receiverId)
        intent.putExtra(Constants.REC_NAME, receiverName)
        intent.putExtra(Constants.REC_PROFILE, receiverProfile)
        intent.putExtra(Constants.CALL_TYPE, type)
        intent.putExtra(Constants.STATUS, "Call_Send")
        intent.putExtra(Constants.ROOM_NAME, UtilsDefault.getRandomString(10))
        startActivity(intent)
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

        bind.txtSearch.setOnClickListener {
            binding.ilHeader.rlChat.visibility = View.GONE
            binding.llSearchView.visibility = View.VISIBLE
        }

        bind.txtMute.setOnClickListener{
            startActivity(Intent(this, PrivateChatInfoActivity::class.java)
                .putExtra(Constants.REC_ID, receiverId))
            popupWindow.dismiss()
        }

        bind.txtViewContact.setOnClickListener {
            startActivity(Intent(this, PrivateChatInfoActivity::class.java)
                .putExtra(Constants.REC_ID, receiverId))
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
        popupWindow  = PopupWindow(popupView, width, height, true)
        popupWindow!!.isFocusable = true
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.showAtLocation(v, Gravity.TOP or Gravity.RIGHT, 0, 0)

        val bind = MenuChatMorePopupBinding.bind(popupView)

        if (blkStatus){
            bind.txtBlock.text = resources.getString(R.string.unblk)
        }else{
            bind.txtBlock.text = resources.getString(R.string.block)
        }

        bind.txtBlock.setOnClickListener {
            if (blkStatus){
                userUnblock()
            }else {
                userBlock()
            }
        }

        bind.txtClearChat.setOnClickListener {
            clearChat()
        }

        popupWindow!!.setOnDismissListener(PopupWindow.OnDismissListener {
            binding.ilHeader.imgMenu.visibility = View.VISIBLE
        })
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
                runTimePermission!!.requestPermission(arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ), object : RunTimePermission.RunTimePermissionListener {
                    override fun permissionGranted() {
                        // First we need to check availability of play services
                        startActivityForResult(Intent(this@ChatViewActivity,
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
                    imageVideoSelectorDialog = ImageVideoSelectorDialog(this@ChatViewActivity, this)
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

            bind.llAudio.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
                imagePermission {
                    startAudioPicker()
                }

            }

            bind.llLocation.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
                pickLocation()
            }

            bind.llContact.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
                pickContact()
            }

            bind.llFolder.setOnClickListener {
                binding.llMsg.visibility = View.VISIBLE
                mBottomDialogDocument!!.dismiss()
                startActivity(Intent(this,CloudActivity::class.java))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pickLocation() {
        val intent = Intent(this, MapActivity::class.java)
        val bundle = Bundle()
        bundle.putString(SimplePlacePicker.API_KEY, "AIzaSyCoezJQ7_58c0bLHXF5wBCjA8-5W0BzJ30")
        intent.putExtras(bundle)
        //noinspection deprecation
        startActivityForResult(intent, SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE)
    }

    private fun pickContact() {
        Dexter.withContext(this)
            .withPermission(permission.READ_CONTACTS)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    MultiContactPicker.Builder(this@ChatViewActivity)
                        .handleColor(ContextCompat.getColor(this@ChatViewActivity,
                            R.color.color_green))
                        .bubbleColor(ContextCompat.getColor(this@ChatViewActivity,
                            R.color.color_green))
                        .showPickerForResult(PICK_CONTACT_REQUEST)
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {}
                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken,
                ) {
                    toast(resources.getString(R.string.missing_permissions))
                }
            })
            .check()
    }

    private fun startAudioPicker() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, PICK_MUSIC_REQUEST)
    }

    private fun startFilePicker() {
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickerIntent.type = "*/*"
        pickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        filePickerLauncher.launch(pickerIntent)
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::onFilePickerResult
    )

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
                try {
                    val imagePath = FileUtils.getPath(this, uri!!)
                    val files = File(imagePath)
                    val requestBody =
                        files.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val file = MultipartBody.Part.createFormData("file", files.name, requestBody)
                    val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val accessToken: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    fileUpload(user_id, accessToken, file,Constants.DOCS)

                }catch (e: Exception) {
                    Log.d("TAG", "onFilePickerResult: " + e)
                }
            }
            else -> return
        }
    }

    private fun fileUpload(
        userId: RequestBody,
        accessToken: RequestBody,
        file: MultipartBody.Part,
        docs: String,
    ) {
        apiViewModel.fileUpload(userId, accessToken, file).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        if (it.data!!.status) {
                            sendImage(it.data.filepath,docs)
                        } else {
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        toast(it.message!!)
                    }
                }
            }
        })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            124 -> {
                if (data != null) {
                    val value = data.getStringExtra("file_url")
                    if (UtilsDefault.isImageFile(value!!)) {
                        sendImage(value,Constants.IMAGE)
                    }else{
                        sendImage(value,Constants.VIDEO)
                    }

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

            PICK_MUSIC_REQUEST -> {
                val uri = data!!.data
                val audioArray: Array<String?>? = UtilsDefault.getAudioPathAndDuration(this, uri)
                if (audioArray == null) {
                    Toast.makeText(this, R.string.could_not_get_audio_file, Toast.LENGTH_SHORT)
                        .show()
                    return
                }

                if (BuildVerUtil.isApi29OrAbove()) {
                    sendAudio(uri.toString(), audioArray[1])
                } else {
                    sendAudio(audioArray[0]!!, audioArray[1])
                }
            }

            PICK_CONTACT_REQUEST -> {
                if (data != null) {
                    try {
                        //get selected contacts from Phonebook
                        val results: List<ContactResult> = MultiContactPicker.obtainResult(data)
                        //convert results to expandableList so the user can choose which numbers he wants to send
                        val contactNameList: List<ExpandableContact> =
                            ContactUtils.getContactsFromContactResult(results)
                        val intent = Intent(this, SelectContactNumbersActivity::class.java)
                        intent.putParcelableArrayListExtra(Constants.EXTRA_CONTACT_LIST,
                            contactNameList as java.util.ArrayList<out Parcelable?>)
                        startActivityForResult(intent, PICK_NUMBERS_FOR_CONTACT_REQUEST)
                    } catch (e: Exception) {
                        Log.d("TAG", "onActivityResult: " + e)
                    }
                }

            }

            PICK_NUMBERS_FOR_CONTACT_REQUEST -> {

                if (data != null) {
                    //get contacts after the user selects the numbers he wants to send
                    val selectedContacts: List<ExpandableContact>? =
                        data.getParcelableArrayListExtra(Constants.EXTRA_CONTACT_LIST)
                    sendContacts(selectedContacts)
                }
            }

            SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE -> {
                if (data != null) {
                    val lon = data.getDoubleExtra(SimplePlacePicker.LOCATION_LNG_EXTRA, 0.0)
                    val lat = data.getDoubleExtra(SimplePlacePicker.LOCATION_LAT_EXTRA, 0.0)

                }
            }

            else -> {
                imageVideoSelectorDialog?.processActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun sendContacts(selectedContacts: List<MultiCheckExpandableGroup>?) {
        if (!selectedContacts.isNullOrEmpty()){
            for (i in 0 until selectedContacts.size){
                Log.d("TAG", "sendContacts: "+selectedContacts[i].title)
            }
        }
    }

    private fun sendAudio(path: String, duration: String?) {
        val imagePath = FileUtils.getPath(this, Uri.parse(path))
        val files = File(imagePath)
        val requestBody =
            files.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val file = MultipartBody.Part.createFormData("file", files.name, requestBody)
        val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val accessToken: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
            .toRequestBody("text/plain".toMediaTypeOrNull())
        fileUpload(user_id, accessToken, file,Constants.AUDIO)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

            }else{
                if (messageSocketModel.data.user_block_status.equals(0)){
                    blkStatus = false
                }else{
                    blkStatus = true
                }
//                if (messageSocketModel.data.id.equals(receiverId)) {
                if (!messageSocketModel.data.list.isNullOrEmpty()) {
                    setData(messageSocketModel.data.list)
                }
//                }
            }
        } else {
            toast(messageSocketModel.message)
        }
    }

    private fun setData(list: ArrayList<ChatDetailsRes>){
        this.list = list
        chatHistoryAdapter!!.setChat(list)
        layoutManager!!.scrollToPosition(chatHistoryAdapter!!.itemCount - 1)
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

    @SuppressLint("Range")
    private fun prepareFilePart(s: String, uri: Uri?): MultipartBody.Part {
        val path = FileUtils.getPath(this, uri)
        val file = File(path)
        if (UtilsDefault.isImageFile(path)) {
            val compressedImageFile = Compressor(this).setQuality(100).compressToFile(file)
            val requestFile: RequestBody =
                compressedImageFile.asRequestBody("*/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(s, compressedImageFile.name, requestFile)
        }

        val requestFile: RequestBody = file.asRequestBody("*/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(s, file.name, requestFile)
    }

    fun sendTypingIndicator(indicate: Boolean) {
        if (!blkStatus) {
            if (indicate) {
                typingEmit("1")
            } else {
                typingEmit("0")
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingEvent(event: TypingEvent) {
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
                if (typingModel.user_id.equals(receiverId)) {
                    if (typingModel.typing.equals("1")) {
                        binding.ilHeader.txtStatus.visibility = View.GONE
                        binding.ilHeader.txtTypingStatus.visibility = View.VISIBLE
                    } else {
                        binding.ilHeader.txtStatus.visibility = View.VISIBLE
                        binding.ilHeader.txtTypingStatus.visibility = View.GONE
                    }
                }
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
            binding.ilHeader.txtStatus.text = UtilsDefault.dateLastSeen(onlineSocketModel.last_seen)
            Log.d("TAG",
                "setOnlineEvent: " + UtilsDefault.dateLastSeen(onlineSocketModel.last_seen))
        } else {
            binding.ilHeader.txtStatus.text = "Online"
        }
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

    private fun userUnblock() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiverId

        apiViewModel.userUnblock(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            blkStatus = false
                            popupWindow!!.dismiss()
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

    private fun userBlock() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiverId

        apiViewModel.userBlock(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            blkStatus = true
                            popupWindow!!.dismiss()
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

    private fun clearChat() {
        val inputParams = InputParams()
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.receiver_id = receiverId

        apiViewModel.chatClear(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            popupWindow!!.dismiss()
                            getChatDetails()
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

}