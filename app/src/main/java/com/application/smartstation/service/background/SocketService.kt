package com.application.smartstation.service.background

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.application.smartstation.BuildConfig
import com.application.smartstation.viewmodel.ChatEvent
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
        socket!!.connect()

        socket!!.on(Socket.EVENT_CONNECT){
            Log.d("TAG", "connect: "+socket!!.connected().toString())
        }

        socket!!.on(Socket.EVENT_DISCONNECT){
            Log.d("TAG", "connect: "+socket!!.connected().toString())
        }

        socket!!.on("message") { args ->
            var jsonObject = JSONObject()
            jsonObject = args[0] as JSONObject
            Log.d("TAG", "onStartCommand: "+jsonObject)
            EventBus.getDefault().post(ChatEvent(jsonObject))
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

    }
}