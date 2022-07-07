package com.application.smartstation.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.application.smartstation.BuildConfig
import com.application.smartstation.R
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilInterface
import com.application.smartstation.util.ViewHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_imageselect.view.*
import java.io.File
import java.io.IOException


class ImageVideoSelectorDialog {

    constructor(activity: Activity, action: Action){
        this.activity = activity
        this.action = action
        create()
    }

    constructor(fragment: Fragment, action: Action){
        this.fragment = fragment
        this.action = action
        create()
    }

    var activity: Activity? = null
    var fragment: Fragment? = null
    var action: Action
    var currentPhotoPath :String? = null

    private fun create(){
        currentPhotoPath = String()

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"

        val chooser = Intent.createChooser(intent, "Choose a Picture")
        activity?.let { it.startActivityForResult(chooser, GALLERY) }
        fragment?.let { it.startActivityForResult(chooser, GALLERY) }

    }


    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        when(resultCode == Activity.RESULT_OK){
            true -> {
                when (requestCode) {
                    GALLERY -> {
                        if (data!=null){
                            val uri = data.data
                            val imagePath = FileUtils.getPath(getContext(),uri!!)
                            if (!TextUtils.isEmpty(imagePath)){
                                val file = File(imagePath!!)
                                imagePath.let {
                                    action.onImageSelected(imagePath,file.name)
                                }
                            }
                        }
                        else{
                            ((activity ?: fragment) as UtilInterface).toast("Image or Video not Selected!")
                        }

                    }
                }
            }
            else -> {
                ((activity ?: fragment) as UtilInterface).toast("Image or Video not Selected!")
            }
        }
    }



    private fun getContext(): Context = (activity ?: fragment!!.context) as Context


    companion object{

        val GALLERY = 2

    }

    interface Action{
        fun onImageSelected(imagePath: String,filename:String)
    }
}