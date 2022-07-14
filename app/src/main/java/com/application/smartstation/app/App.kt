package com.application.smartstation.app


import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONObject


@HiltAndroidApp
class App : MultiDexApplication(){
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MultiDex.install(this)
        EmojiManager.install(IosEmojiProvider())
    }

}