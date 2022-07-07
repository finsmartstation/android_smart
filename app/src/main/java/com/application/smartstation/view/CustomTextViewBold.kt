package com.application.smartstation.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class CustomTextViewBold : androidx.appcompat.widget.AppCompatTextView{

    constructor(context: Context) : super(context){
        setFont()
    }
    constructor(context: Context, attrs : AttributeSet) : super(context,attrs){
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        setFont()
    }

    private fun setFont() {

        val font = Typeface.createFromAsset(context.assets, "fontName/dinbold.ttf")

        setTypeface(font, Typeface.BOLD)
    }
}