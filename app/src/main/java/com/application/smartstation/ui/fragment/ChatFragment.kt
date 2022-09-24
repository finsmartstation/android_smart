package com.application.smartstation.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentChatBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.activity.ChatActivity
import com.application.smartstation.ui.activity.MainActivity
import com.application.smartstation.ui.adapter.ChatAdapter
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class ChatFragment : BaseFragment(R.layout.fragment_chat){

    private val binding by viewBinding(FragmentChatBinding::bind)

    var list:ArrayList<DataChatList> = ArrayList()
    var chatAdapter: ChatAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()
    val mainHandler = Handler(Looper.getMainLooper())
    var emitters: SocketService.Emitters? = null
    var contactList:ArrayList<ContactListRes> = ArrayList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        emitters = SocketService.Emitters(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.rvChat.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        chatAdapter = ChatAdapter(requireActivity())
        binding.rvChat.adapter = chatAdapter

        chatAdapter!!.onItemClick = { model ->
            startActivity(Intent(requireActivity(), ChatActivity::class.java).putExtra(Constants.REC_ID,model.userid).putExtra(Constants.NAME,model.name).putExtra(Constants.PROFILE,model.profile).putExtra(Constants.CHAT_TYPE,model.chat_type).putExtra(Constants.ROOM,model.room))
        }

        phnPermission{
            getContactList()
        }

        emitRecentChat()

//        binding.edtSearch.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val txt = s.toString()
//                filterList(txt)
//
//            }
//        })

        (activity as MainActivity?)!!.setSendData(object : MainActivity.SearchInterface {
            override fun searchData(searchTxt: String, type: String) {
                if (type.equals("chat")) {
                    filterList(searchTxt)
                }
            }

        })

    }

    @SuppressLint("Range")
    private fun getContactList() {
        try {
            val cr = requireActivity().contentResolver
            val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null)
            if ((if (cur != null) cur.getCount() else 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    val id: String = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))
                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                    ) {
                        val pCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null)
                        while (pCur!!.moveToNext()) {
                            val phoneNo: String = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactList.add(ContactListRes(name,phoneNo))
                        }
                        pCur.close()
                    }
                }
            }
            if (cur != null) {
                cur.close()
            }
        }catch (e:Exception){
            Log.d("TAG", "getContactList: "+e)
        }

    }

    private fun getChatList() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getChatlist(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.data
                            if(list.isNotEmpty()) {
                                binding.rvChat.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            }else{
                                binding.rvChat.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setData(list: ArrayList<DataChatList>) {
            for (a in contactList) {
                for (b in 0 until list.size) {
                    if (PhoneNumberUtils.compare(a.Phn, list[b].phone)) {
                        list.set(b,DataChatList(list[b].id,
                            list[b].date,
                            list[b].message,
                            list[b].phone,
                            list[b].message_type,
                            list[b].unread_message,
                            list[b].userid
                            ,a.name
                            ,list[b].profile
                            ,list[b].room
                            ,list[b].chat_type))
                    }
                }
            }
            chatAdapter!!.setChat(list)
    }

    override fun onResume() {
        super.onResume()
        getChatList()
    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<DataChatList> = ArrayList()

            for (items in list) {
                val chat = items.name.toLowerCase()+items.message.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }
            chatAdapter?.setChat(templist)
        } else {
            chatAdapter?.setChat(list)
        }
    }

    private fun setOnClickListener() {

    }

    fun emitRecentChat(){
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("user_id", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("accessToken", UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                emitters!!.recent_chat_emit(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecentChatEvent(event: RecentChatEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setRecentEvent(jsonObject)
        Log.d("TAG", "ONCHATEVENT: "+jsonObject)
    }

    private fun setRecentEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val messageSocketModel: GetChatListResponse = gson.fromJson(jsonObject.toString(),
            GetChatListResponse::class.java)
        if (messageSocketModel.status){
            if (messageSocketModel.data.isNotEmpty()){
                setData(messageSocketModel.data)
            }
        }else{
            toast(messageSocketModel.message)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTypingUserIdEvent(event: TypingUserIdEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setTypingEvent(jsonObject)
        Log.d("TAG", "TYPINGEVENT: "+jsonObject)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onTypingEventGrp(event: TypingEventGrp) {
//        var jsonObject: JSONObject? = JSONObject()
//        jsonObject = event.getJsonObject()
//        setTypingEvent(jsonObject)
//        Log.d("TAG", "TYPINGEVENT: "+jsonObject)
//    }

    fun setTypingEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val typingModel: TypingRes = gson.fromJson(jsonObject.toString(),
            TypingRes::class.java)
        if(typingModel.status){
            for (i in 0 until list.size){
                if (list[i].userid.equals(typingModel.user_id)){
                    if(typingModel.typing.equals("1")){
                        chatAdapter?.setChats(1,i)
                    } else{
                        chatAdapter?.setChats(0,i)
                    }
                }
            }

        }
    }


}