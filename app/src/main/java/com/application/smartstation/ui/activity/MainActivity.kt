package com.application.smartstation.ui.activity

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMainBinding
import com.application.smartstation.databinding.BottomSheetDialogMainBinding
import com.application.smartstation.databinding.DialogLogoutBinding
import com.application.smartstation.databinding.MenuPopupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.activity.product.ProductActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.adapter.ContactAdapter
import com.application.smartstation.ui.fragment.*
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.ui.model.ContactListRes
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject


@AndroidEntryPoint
class MainActivity : BaseActivity(), InboxFragment.OnUnreadMailCountListener,
    LetterInboxFragment.OnUnreadLetterCountListener {

    val binding: ActivityMainBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var fragmentHelper: FragmentHelper? = null
    var rootref: DatabaseReference? = null
    var type = "chat"
    var searchInterface: SearchInterface? = null
    val chatMainFragment = ChatMainFragment()
    var list: ArrayList<DataUserList> = ArrayList()
    var contactList: ArrayList<ContactListRes> = ArrayList()
    var contactAdapter: ContactAdapter? = null
    var listChat: ArrayList<DataChatList> = java.util.ArrayList()
    var chatAdapter: ChatAdapter? = null
    val emitters: SocketService.Emitters = SocketService.Emitters(this)
    var CAMERA_MIC_PERMISSION_REQUEST_CODE = 791

    interface SearchInterface {
        fun searchData(searchTxt: String, type: String)
    }

    fun setSendData(sendData: SearchInterface) {
        this.searchInterface = sendData
    }

    companion object {
        var statusHome = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        setContentView(R.layout.activity_main)
        initView()
        setUpFragments()
        setOnClickListener()

        offline()
    }

    private fun initView() {

        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone()
        }

        if (!checkPermission()){
            requestPermission()
        }

        binding.rvSearch.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        contactAdapter = ContactAdapter(this)
        binding.rvSearch.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, ChatViewActivity::class.java).putExtra(Constants.REC_ID,
                model.user_id).putExtra(Constants.NAME, model.name)
                .putExtra(Constants.PROFILE, model.profile_pic))
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE

            binding.llTab.visibility = View.VISIBLE
            binding.mainFrameContainer.visibility = View.VISIBLE
            binding.rlBottom.visibility = View.VISIBLE
            binding.imgBack.visibility = View.GONE
            binding.imgMenu.visibility = View.VISIBLE
            binding.llSearchView.visibility = View.GONE

            binding.edtSearch.setText("")
        }

        binding.rvMessage.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        chatAdapter = ChatAdapter(this)
        binding.rvMessage.adapter = chatAdapter

        chatAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, ChatViewActivity::class.java).putExtra(Constants.REC_ID,
                model.userid).putExtra(Constants.NAME, model.name)
                .putExtra(Constants.PROFILE, model.profile))
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE

            binding.llTab.visibility = View.VISIBLE
            binding.mainFrameContainer.visibility = View.VISIBLE
            binding.rlBottom.visibility = View.VISIBLE
            binding.imgBack.visibility = View.GONE
            binding.imgMenu.visibility = View.VISIBLE
            binding.llSearchView.visibility = View.GONE

            binding.edtSearch.setText("")
        }

//        binding.dlView.addDrawerListener(object : SimpleDrawerListener() {
//            override fun onDrawerSlide(drawer: View, slideOffset: Float) {
//                binding.llView.setX(binding.nvMenu.getWidth() * slideOffset)
//                val lp:RelativeLayout.LayoutParams = binding.llView.getLayoutParams() as RelativeLayout.LayoutParams
//                lp.height = drawer.height -
//                        (drawer.height * slideOffset * 0.3f).toInt()
//                lp.topMargin = (drawer.height - lp.height) / 2
//                binding.llView.setLayoutParams(lp)
//            }
//
//            override fun onDrawerClosed(drawerView: View) {}
//        }
//        )

//        binding.nvMenu.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        binding.nvMenu.getHeaderView(0).txtUsername.text = "Fin Smart"

        UtilsDefault.initService(SocketService::class.java, this)

        rootref = FirebaseDatabase.getInstance().reference


        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txt = s.toString()
                if (!type.equals("chat")) {
                    searchInterface!!.searchData(txt, type)
                } else {
                    filterUserList(txt)
                    filterChatList(txt)
                }
            }
        })
    }

    private fun setUpFragments() {
        fragmentHelper = FragmentHelper(supportFragmentManager)
        fragmentHelper?.setUpFrame(chatMainFragment, binding.mainFrameContainer)
    }

    private val mOnNavigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.logout -> {
                    val inputParams = InputParams()
                    inputParams.accessToken =
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                    inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                    logout(inputParams)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun logout(inputParams: InputParams) {
        apiViewModel.logout(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            UtilsDefault.setLoggedIn(this, false)
                            UtilsDefault.clearSession()
                            startActivity(Intent(this, LoginActivity::class.java))
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


    private fun setOnClickListener() {
        binding.rlChat.setOnClickListener {
            binding.rlChat.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlMail.background = null
            binding.rlLetter.background = null
            fragmentHelper?.push(ChatMainFragment())
            binding.imgNewMail.visibility = View.INVISIBLE
            binding.imgPlus.visibility = View.VISIBLE
            binding.llMail.visibility = View.INVISIBLE
            type = "chat"
        }

        binding.imgPlus.setOnClickListener {
            bottomDialog()
        }

        binding.rlMail.setOnClickListener {
            binding.rlMail.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlChat.background = null
            binding.rlLetter.background = null
            fragmentHelper?.push(EmailMainFragment())
            binding.imgNewMail.visibility = View.VISIBLE
            binding.imgPlus.visibility = View.INVISIBLE
            binding.llMail.visibility = View.VISIBLE
            binding.imgMail.setImageResource(R.drawable.ic_mail_main_use)
            type = "mail"
        }

        binding.imgNewMail.setOnClickListener {
            if (type.equals("mail")) {
                startActivity(Intent(this, NewMailActivity::class.java))
            } else {
                startActivity(Intent(this, NewLetterActivity::class.java))
            }
        }

        binding.rlLetter.setOnClickListener {
            binding.rlLetter.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlMail.background = null
            binding.rlChat.background = null
            fragmentHelper?.push(LetterMainFragment())
            binding.imgNewMail.visibility = View.VISIBLE
            binding.imgPlus.visibility = View.INVISIBLE
            binding.llMail.visibility = View.VISIBLE
            binding.imgMail.setImageResource(R.drawable.ic_letter_24)
            type = "letter"
        }

        binding.imgSearch.setOnClickListener {
            binding.llSearch.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.imgCancel.visibility = View.VISIBLE

            if (type.equals("chat")) {
            binding.llTab.visibility = View.GONE
            binding.mainFrameContainer.visibility = View.GONE
            binding.rlBottom.visibility = View.GONE
            binding.imgBack.visibility = View.VISIBLE
            binding.imgMenu.visibility = View.GONE
            binding.llSearchView.visibility = View.VISIBLE

                getUserDetails()
                getChatList()
            }
        }

        binding.imgBack.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE

            binding.llTab.visibility = View.VISIBLE
            binding.mainFrameContainer.visibility = View.VISIBLE
            binding.rlBottom.visibility = View.VISIBLE
            binding.imgBack.visibility = View.GONE
            binding.imgMenu.visibility = View.VISIBLE
            binding.llSearchView.visibility = View.GONE

            binding.edtSearch.setText("")

        }

        binding.imgCancel.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE

            binding.llTab.visibility = View.VISIBLE
            binding.mainFrameContainer.visibility = View.VISIBLE
            binding.rlBottom.visibility = View.VISIBLE
            binding.imgBack.visibility = View.GONE
            binding.imgMenu.visibility = View.VISIBLE
            binding.llSearchView.visibility = View.GONE

            binding.edtSearch.setText("")
        }

        binding.imgMenu.setOnClickListener { v ->
            binding.imgMenu.visibility = View.GONE
            menuPopup(v)
        }
    }

    private fun bottomDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_main, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.setOnShowListener { setupBottomSheet(it) }
        val bind = BottomSheetDialogMainBinding.bind(view)
        bind.llCloud.setOnClickListener {
            startActivity(Intent(this, CloudActivity::class.java))
            dialog.dismiss()
        }
        bind.llProduct.setOnClickListener {
            startActivity(Intent(this, ProductActivity::class.java))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupBottomSheet(it: DialogInterface?) {
        val bottomSheetDialog = it as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    fun menuPopup(v: View) {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.menu_popup, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.LEFT, -100, -50)

        val bind = MenuPopupBinding.bind(popupView)

        if (type.equals("chat")) {
            bind.txtNewGrp.visibility = View.VISIBLE
            bind.imgNewGrp.visibility = View.VISIBLE
            bind.txtStarMsg.visibility = View.GONE
            bind.imgStarMsg.visibility = View.GONE
            bind.txtSendMail.visibility = View.GONE
            bind.imgSent.visibility = View.GONE
        } else {
            bind.txtNewGrp.visibility = View.GONE
            bind.imgNewGrp.visibility = View.GONE
            bind.txtStarMsg.visibility = View.GONE
            bind.imgStarMsg.visibility = View.GONE
            bind.txtSendMail.visibility = View.VISIBLE
            bind.imgSent.visibility = View.VISIBLE
        }

        bind.txtSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
        }

        bind.txtNewGrp.setOnClickListener {
            startActivity(Intent(this, CreateGroupActivity::class.java))
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
        }

        bind.txtLogout.setOnClickListener {
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
            logoutDialog()
        }

        bind.txtSendMail.setOnClickListener {
            if (type.equals("mail")) {
                startActivity(Intent(this, SentMailActivity::class.java))
            } else {
                startActivity(Intent(this, SentLetterActivity::class.java))
            }
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
        }

        popupWindow.setOnDismissListener(PopupWindow.OnDismissListener {
            binding.imgMenu.visibility = View.VISIBLE
        })

    }

    private fun logoutDialog() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_logout, null)
            val dialogLogout = Dialog(this)
            val window: Window = dialogLogout.window!!
            window.setGravity(Gravity.CENTER)
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.FILL_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val bind = DialogLogoutBinding.bind(view)
            dialogLogout.setCancelable(false)

            dialogLogout.setContentView(view)
            dialogLogout.show()

            bind.txtYes.setOnClickListener {
                dialogLogout.dismiss()
                val inputParams = InputParams()
                inputParams.accessToken =
                    UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                logout(inputParams)
            }

            bind.txtNo.setOnClickListener {
                dialogLogout.dismiss()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        rootref!!.child("Users")
            .child(UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!)
            .child("token")
            .setValue(UtilsDefault.getSharedPreferenceValuefcm(Constants.FCM_KEY)!!)
    }

    private fun getUserDetails() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getUserlist(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
//                        showProgress()
                    }
                    Status.SUCCESS -> {
//                        dismissProgress()
                        if (it.data!!.status) {
                            var list1 = it.data.data
                            if (list1.isNotEmpty()) {
//                                list.clear()
//                                for (a in contactList) {
//                                    for (b in list1) {
//                                        if (PhoneNumberUtils.compare(a.Phn, b.phone)) {
//                                            list.add(DataUserList(b.user_id,
//                                                a.name,
//                                                b.profile_pic,
//                                                b.phone,
//                                                b.country,
//                                                b.about))
//                                        }
//                                    }
//                                }
                                setData(list1)
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

    private fun setData(list: ArrayList<DataUserList>) {
        contactAdapter!!.setChat(list)
    }

    private fun filterUserList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: java.util.ArrayList<DataUserList> = java.util.ArrayList()

            for (items in list) {
                val chat = items.name.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            contactAdapter?.setChat(templist)
        } else {
            contactAdapter?.setChat(list)
        }
    }

    private fun getChatList() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        try {
            apiViewModel.getChatlist(inputParams).observe(this, Observer {
                it.let {
                    when (it.status) {
                        Status.LOADING -> {
                        }
                        Status.SUCCESS -> {
                            if (it.data!!.status) {
                                listChat = it.data.data
                                if (listChat.isNotEmpty()) {
                                    setDataChat(listChat)
                                }
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
        } catch (e: Exception) {
            Log.d("TAG", "getChatListRefresh: " + e)
        }

    }

    private fun setDataChat(listChat: ArrayList<DataChatList>) {
        chatAdapter!!.setChat(listChat)
    }

    private fun filterChatList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: java.util.ArrayList<DataChatList> = java.util.ArrayList()

            for (items in listChat) {
                val chat = items.name.toLowerCase() + items.message.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            if (templist.isNotEmpty()) {
                binding.llMsgList.visibility = View.VISIBLE
            } else {
                binding.llMsgList.visibility = View.GONE
            }
            chatAdapter?.setChat(templist)
        } else {
            binding.llMsgList.visibility = View.GONE
            chatAdapter?.setChat(listChat)
        }
    }

    override fun onDestroy() {
        offline()
        super.onDestroy()
    }

    fun offline() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("s_id", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
            Log.d("TAG", "offline: " + jsonObject)
//            emitters.offline(jsonObject)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onUnreadMail(count: String) {
        if (count.toInt() == 0) {
            binding.llUnread.visibility = View.INVISIBLE
        } else if (count.toInt() < 100) {
            binding.llUnread.visibility = View.VISIBLE
            binding.txtUnreadCount.text = count
        } else {
            binding.llUnread.visibility = View.VISIBLE
            binding.txtUnreadCount.text = "99+"
        }
    }

    override fun onUnreadLetter(count: String) {
        if (count.toInt() == 0) {
            binding.llUnread.visibility = View.INVISIBLE
        } else if (count.toInt() < 100) {
            binding.llUnread.visibility = View.VISIBLE
            binding.txtUnreadCount.text = count
        } else {
            binding.llUnread.visibility = View.VISIBLE
            binding.txtUnreadCount.text = "99+"
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

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                2296)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2296 -> if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}