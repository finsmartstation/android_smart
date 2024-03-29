package com.application.smartstation.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class CustomEmojiEditText : com.vanniktech.emoji.EmojiEditText {

    constructor(context: Context) : super(context) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        setFont()
    }

    private fun setFont() {
        val font = Typeface.createFromAsset(context.assets, "fontName/dinregular.ttf")
        setTypeface(font, Typeface.NORMAL)
    }
}