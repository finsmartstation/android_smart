package com.application.smartstation.ui.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMainBinding
import com.application.smartstation.databinding.DialogLogoutBinding
import com.application.smartstation.databinding.MenuPopupBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.adapter.ContactAdapter
import com.application.smartstation.ui.fragment.ChatMainFragment
import com.application.smartstation.ui.fragment.EmailMainFragment
import com.application.smartstation.ui.fragment.LetterMainFragment
import com.application.smartstation.ui.helper.FragmentHelper
import com.application.smartstation.ui.model.DataChatList
import com.application.smartstation.ui.model.DataUserList
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.nav_header_main.view.*


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    val binding: ActivityMainBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var fragmentHelper: FragmentHelper? = null
    var mBottomDialogLogout: Dialog? = null
    var rootref: DatabaseReference? = null
    var type = "chat"
    var searchInterface:SearchInterface? = null
    val chatMainFragment = ChatMainFragment()
    var list:ArrayList<DataUserList> = ArrayList()
    var contactAdapter: ContactAdapter? = null
    var listChat: ArrayList<DataChatList> = java.util.ArrayList()
    var chatAdapter: ChatAdapter? = null

    interface SearchInterface {
        fun searchData(searchTxt:String,type:String)
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
    }

    private fun initView() {

        binding.rvSearch.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        contactAdapter = ContactAdapter(this)
        binding.rvSearch.adapter = contactAdapter

        contactAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, ChatActivity::class.java).putExtra(Constants.REC_ID,model.user_id).putExtra(Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile_pic))
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

        binding.rvMessage.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        chatAdapter = ChatAdapter(this)
        binding.rvMessage.adapter = chatAdapter

        chatAdapter!!.onItemClick = { model ->
            startActivity(Intent(this, ChatActivity::class.java).putExtra(Constants.REC_ID,model.userid).putExtra(Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile))
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

        binding.dlView.addDrawerListener(object : SimpleDrawerListener() {
            override fun onDrawerSlide(drawer: View, slideOffset: Float) {
                binding.llView.setX(binding.nvMenu.getWidth() * slideOffset)
                val lp:RelativeLayout.LayoutParams = binding.llView.getLayoutParams() as RelativeLayout.LayoutParams
                lp.height = drawer.height -
                        (drawer.height * slideOffset * 0.3f).toInt()
                lp.topMargin = (drawer.height - lp.height) / 2
                binding.llView.setLayoutParams(lp)
            }

            override fun onDrawerClosed(drawerView: View) {}
        }
        )

        binding.nvMenu.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        binding.nvMenu.getHeaderView(0).txtUsername.text = "Fin Smart"

        UtilsDefault.initService(SocketService::class.java,this)

        rootref = FirebaseDatabase.getInstance().reference


        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txt = s.toString()
                if(!type.equals("chat")) {
                    searchInterface!!.searchData(txt, type)
                }else{
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
                    startActivity(Intent(this,SettingsActivity::class.java))
                }
                R.id.logout -> {
                    var inputParams = InputParams()
                    inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
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
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            UtilsDefault.setLoggedIn(this,false)
                            UtilsDefault.clearSession()
                            startActivity(Intent(this,LoginActivity::class.java))
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

        binding.rlMail.setOnClickListener {
            binding.rlMail.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlChat.background = null
            binding.rlLetter.background = null
            fragmentHelper?.push(EmailMainFragment())
            binding.imgNewMail.visibility = View.VISIBLE
            binding.imgPlus.visibility = View.INVISIBLE
            binding.llMail.visibility = View.VISIBLE
            type = "mail"
        }

        binding.imgNewMail.setOnClickListener {
            startActivity(Intent(this,NewMailActivity::class.java))
        }

        binding.rlLetter.setOnClickListener {
            binding.rlLetter.background = getDrawable(R.color.color_tab_select_bg)
            binding.rlMail.background = null
            binding.rlChat.background = null
            fragmentHelper?.push(LetterMainFragment())
            type = "letter"
        }

        binding.imgSearch.setOnClickListener {
            binding.llSearch.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.imgCancel.visibility = View.VISIBLE

            binding.llTab.visibility = View.GONE
            binding.mainFrameContainer.visibility = View.GONE
            binding.rlBottom.visibility = View.GONE
            binding.imgBack.visibility = View.VISIBLE
            binding.imgMenu.visibility = View.GONE
            binding.llSearchView.visibility = View.VISIBLE

            getUserDetails()
            getChatList()
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

        binding.imgMenu.setOnClickListener {v ->
//            if (binding.dlView.isDrawerOpen(binding.nvMenu)) {
//                binding.dlView.closeDrawer(binding.nvMenu)
//            } else {
//                binding.dlView.openDrawer(binding.nvMenu)
//            }
            binding.imgMenu.visibility = View.INVISIBLE
            menuPopup(v)
        }

        binding.nvMenu.getHeaderView(0).imgNavBack.setOnClickListener {
            if (binding.dlView.isDrawerOpen(binding.nvMenu)) {
                binding.dlView.closeDrawer(binding.nvMenu)
            }
        }
    }

    fun menuPopup(v: View) {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.menu_popup, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setFocusable(true)
        popupWindow.setOutsideTouchable(true)
        popupWindow.showAtLocation(v, Gravity.TOP or Gravity.LEFT, -100, -50)

        val bind = MenuPopupBinding.bind(popupView)

        bind.txtSettings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
        }

        bind.txtLogout.setOnClickListener {
            binding.imgMenu.visibility = View.VISIBLE
            popupWindow.dismiss()
            logoutDialog()
        }

        bind.txtAddContact.setOnClickListener {
            startActivity(Intent(this,UserDetailsActivity::class.java))
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
            val window: Window = dialogLogout.getWindow()!!
            window.setGravity(Gravity.CENTER)
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val bind = DialogLogoutBinding.bind(view)
            dialogLogout.setCancelable(false)

            dialogLogout.setContentView(view)
            dialogLogout.show()

            bind.txtYes.setOnClickListener {
                dialogLogout.dismiss()
                val inputParams = InputParams()
                inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
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
                when(it.status){
                    Status.LOADING -> {
//                        showProgress()
                    }
                    Status.SUCCESS -> {
//                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.data
                            if(list.isNotEmpty()) {
                                setData(list)
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
                    when(it.status){
                        Status.LOADING -> {
                        }
                        Status.SUCCESS -> {
                            if (it.data!!.status){
                                listChat = it.data.data
                                if(listChat.isNotEmpty()) {
                                    setDataChat(listChat)
                                }
                            }else{
                                toast(it.data.message)
                            }
                        }
                        Status.ERROR -> {
                            toast(it.message!!)
                        }
                    }
                }
            })
        }catch (e:Exception){
            Log.d("TAG", "getChatListRefresh: "+e)
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
                val chat = items.name.toLowerCase()+items.message.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            if (templist.isNotEmpty()){
                binding.llMsgList.visibility = View.VISIBLE
            }else{
                binding.llMsgList.visibility = View.GONE
            }
            chatAdapter?.setChat(templist)
        } else {
            binding.llMsgList.visibility = View.GONE
            chatAdapter?.setChat(listChat)
        }
    }
}