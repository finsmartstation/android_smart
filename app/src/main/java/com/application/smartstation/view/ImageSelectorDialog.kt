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


class ImageSelectorDialog {

    constructor(activity: Activity, action: Action,typeImage:String){
        this.activity = activity
        this.action = action
        this.typeImage = typeImage
        create()
    }

    constructor(fragment: Fragment, action: Action,typeImage:String){
        this.fragment = fragment
        this.action = action
        this.typeImage = typeImage
        create()
    }

    var activity: Activity? = null
    var fragment: Fragment? = null
    var typeImage: String? = null
    var action: Action
    var dialog: BottomSheetDialog? = null
    var currentPhotoPath :String? = null

    private fun create(){
        dialog = BottomSheetDialog(getContext())
        currentPhotoPath = String()

        val view = ViewHelper.createView(getContext(), R.layout.dialog_imageselect)

        dialog!!.setOnShowListener {
            (view.parent as View).setBackgroundColor(Color.TRANSPARENT)
        }

        val cameraClick = View.OnClickListener {
            dialog?.dismiss()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    getContext(),
                    "${BuildConfig.APPLICATION_ID}.provider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                // activity?.startActivityForResult(cameraIntent, CAMERA)
            }

            activity?.let { it.startActivityForResult(cameraIntent, CAMERA) }
            fragment?.let { it.startActivityForResult(cameraIntent, CAMERA) }
        }

        val galleryClick = View.OnClickListener {
            dialog?.dismiss()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            val chooser = Intent.createChooser(intent, "Choose a Picture")
            activity?.let { it.startActivityForResult(chooser, GALLERY) }
            fragment?.let { it.startActivityForResult(chooser, GALLERY) }
        }

        val cancelClick = View.OnClickListener {
            dialog?.dismiss()
        }

        view.rlCamera.setOnClickListener(cameraClick)
        view.rlGallery.setOnClickListener(galleryClick)
        view.textCancel.setOnClickListener(cancelClick)

        dialog?.setContentView(view)
        dialog?.show()
    }

    private fun createImageFile(): File? {
        val current = System.currentTimeMillis()
        val timeStamp = (current / 1000).toString()
        val myStuff =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "com.smartstation"+"/Smart Station/Media/Smart Station $typeImage"
            )
        if (!myStuff.exists())
            myStuff.mkdirs()
        return File.createTempFile(
            "smart_station_"+timeStamp, /* prefix */
            ".jpg", /* suffix */
            myStuff /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Log.d("TAG", "createImageFile: "+currentPhotoPath)
        }

    }


    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        when(resultCode == Activity.RESULT_OK){
            true -> {
                when (requestCode) {
                    CAMERA -> {
                        try {
                            val path = currentPhotoPath
                            val file = File(path)
                            path.let {
                                action.onImageSelected(path!!,file.name)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
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
                            ((activity ?: fragment) as UtilInterface).toast("Image not Selected!")
                        }

                    }
                }
            }
            else -> {
                ((activity ?: fragment) as UtilInterface).toast("Image not Selected!")
            }
        }
    }



    private fun getContext(): Context = (activity ?: fragment!!.context) as Context


    companion object{

        val CAMERA = 1
        val GALLERY = 2

    }

    interface Action{
        fun onImageSelected(imagePath: String,filename:String)
    }
}