package com.application.smartstation.service.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.application.smartstation.BuildConfig
import com.application.smartstation.viewmodel.*
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.net.URISyntaxException


class SocketService: Service() {

    val SOCKET_URL = BuildConfig.SOCKET_BASE_URL+"/"

    companion object {
        var socket: Socket? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val opts = IO.Options()
        opts.forceNew = true
        opts.reconnection = true

        try {
            socket = IO.socket(SOCKET_URL, opts)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        socket!!.on(Socket.EVENT_CONNECT){
            Log.d("TAG", "connect: "+socket!!.connected().toString())
        }

        socket!!.on(Socket.EVENT_DISCONNECT){
            Log.d("TAG", "connect: "+socket!!.connected().toString())
        }

        socket!!.on("message") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "msg: "+jsonObject)
            EventBus.getDefault().post(ChatEvent(jsonObject))
        }

        socket!!.on("chat_list") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "recent: "+jsonObject)
            EventBus.getDefault().post(RecentChatEvent(jsonObject))
        }

        socket!!.on("online_users") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "online: "+jsonObject)
            EventBus.getDefault().post(OnlineChatEvent(jsonObject))
        }

        socket!!.on("typing_individual_room") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "typing: "+jsonObject)
            EventBus.getDefault().post(TypingEvent(jsonObject))
        }

        socket!!.on("typing_individual_chatlist") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "typing_ind: "+jsonObject)
            EventBus.getDefault().post(TypingEvent(jsonObject))
        }

        socket!!.on("typing_group") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "typing_ind: "+jsonObject)
            EventBus.getDefault().post(TypingEvent(jsonObject))
        }

        socket!!.on("mail_inboxlist") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "inbox: "+args)
            EventBus.getDefault().post(InboxEvent(jsonObject))
        }

        socket!!.on("send_mail_list") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "send: "+jsonObject)
            EventBus.getDefault().post(SentboxEvent(jsonObject))
        }
        
        return START_STICKY
    }

    class Emitters(context: Context?) {

        fun sendMessage(jsonObject: JSONObject) {
            socket!!.emit("message", jsonObject)
        }

        fun room(jsonObject: JSONObject) {
            socket!!.emit("room", jsonObject)
        }

        fun recent_chat_emit(jsonObject: JSONObject) {
            socket!!.emit("chat_list", jsonObject)
        }

        fun offline(jsonObject: JSONObject) {
            socket!!.emit("dis", jsonObject)
        }

        fun typingStatus(jsonObject: JSONObject) {
            socket!!.emit("typing_individual", jsonObject)
        }

        fun typingGrpStatus(jsonObject: JSONObject) {
            socket!!.emit("type_group", jsonObject)
        }

        fun mailList(jsonObject: JSONObject) {
            socket!!.emit("mail_inboxlist", jsonObject)
        }

        fun sendMailList(jsonObject: JSONObject) {
            socket!!.emit("send_mail_list", jsonObject)
        }

    }
}