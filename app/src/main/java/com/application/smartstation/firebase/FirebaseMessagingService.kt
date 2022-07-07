package com.application.smartstation.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.application.smartstation.R
import com.application.smartstation.ui.activity.CallActivity
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

open class FirebaseMessagingService: FirebaseMessagingService() {

    var deviceToken: String = ""
    var title: String = ""
    var message:String = ""
    var intent: Intent? = null
    var TAG = "FirebaseMessaging"
    var type = ""
    var action = ""
    var id = ""
    var image = ""
    var roomName = ""
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isComplete){
                deviceToken = it.result.toString()
                Log.d("fcmtoken", deviceToken)
                UtilsDefault.updateSharedPreferenceFCM(Constants.FCM_KEY, deviceToken)
            }
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()){
            message = remoteMessage.data["body"].toString()
            title = remoteMessage.data["title"].toString()
            type = remoteMessage.data["type"].toString()
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }

        if (remoteMessage.notification !=null){
            message = remoteMessage.notification!!.body!!
            title = remoteMessage.notification!!.title!!
//            type = remoteMessage.notification!!.type!!
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }

        try {
//            if (UtilsDefault.getSharedPreferenceString(Constants.PUSHNOTIFICATION) == "enable"){
            if (type != null && (type == "video_call" || type == "voice_call")){
                    if (UtilsDefault.isOnline()){
                        if (remoteMessage.data.isNotEmpty()){
                            action = remoteMessage.data["action"].toString()
                            id = remoteMessage.data["senderId"].toString()
                            image = remoteMessage.data["senderImage"].toString()
                            roomName = remoteMessage.data["message"].toString()

                            if (action == CallActivity.callReceive || CallActivity.is_calling_activity_open) {
                                val i = Intent(this, CallActivity::class.java)
                                i.putExtra(Constants.REC_ID, id)
                                i.putExtra(Constants.REC_NAME, title)
                                i.putExtra(Constants.REC_PROFILE, image)
                                i.putExtra(Constants.STATUS, action)
                                i.putExtra(Constants.CALL_TYPE, type)
                                i.putExtra(Constants.ROOM_NAME, roomName)
                                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(i)
                            }
                        }
                    }
            }else {
                sendNotification(message, title)
                Log.i("onmessage", "message" + message)
            }
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(message: String, title: String) {

        val channelId = "SMART_STATION_CHANNEL_ID"
        val channelName = "SMART_STATION"
        val extras = intent!!.extras
//        val intent = if (message.contains("Kyc")){
//            Intent(this, KYCActivity::class.java)
//        } else {
//            Intent(this, MainActivity::class.java)
//        }
//        intent!!.putExtra("dataNotification", 1)
//        intent!!.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.app_logo))
            .setSmallIcon(R.mipmap.app_logo)
            .setContentTitle(UtilsDefault.checkNull(title))
            .setContentText(UtilsDefault.checkNull(message))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            // .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(ContextCompat.getColor(this, R.color.color_green))
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = message
            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(channelId)
        }
        val notificationID = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
        notificationManager.notify(notificationID, notificationBuilder.build())

    }

    fun getBitmapfromUrl(imageUrl: String): Bitmap? {
        try {
            val url = URL("" + imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            e.printStackTrace()
            return null

        }

    }
}