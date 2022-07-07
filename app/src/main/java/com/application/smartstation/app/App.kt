package com.application.smartstation.app


import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import dagger.hilt.android.HiltAndroidApp


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