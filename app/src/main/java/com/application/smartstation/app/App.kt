package com.application.smartstation.app


import android.content.Context
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.emoji.iosprovider.AXIOSEmojiProvider
import com.application.smartstation.R
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : MultiDexApplication() {
    companion object {
        lateinit var instance: App
            private set

        @JvmStatic
        fun context(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MultiDex.install(this)
        EmojiManager.install(IosEmojiProvider())
        initEmojiKeyboard()
    }

    private fun initEmojiKeyboard() {
        AXEmojiManager.install(this, AXIOSEmojiProvider(this))
    }


}