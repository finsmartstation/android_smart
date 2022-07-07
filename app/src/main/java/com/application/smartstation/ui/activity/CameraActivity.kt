package com.application.smartstation.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.SensorManager
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.widget.Toast
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCameraBinding
import com.application.smartstation.ui.activity.ChatActivity.Companion.RESULT_CODE
import com.application.smartstation.util.Constants
import com.application.smartstation.util.RunTimePermission
import com.application.smartstation.util.viewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CameraActivity : BaseActivity(), SurfaceHolder.Callback {

    val binding: ActivityCameraBinding by viewBinding()

    private var surfaceHolder: SurfaceHolder? = null
    private var camera: Camera? = null
    private val customHandler = Handler()
    var flag = 0
    private var tempFile: File? = null
    private var jpegCallback: Camera.PictureCallback? = null
    var MAX_VIDEO_SIZE_UPLOAD = 100 //MB
    private var folderImg: File? = null
    private var folderVideo: File? = null
    private var mediaRecorder: MediaRecorder? = null
    var myOrientationEventListener: OrientationEventListener? = null
    var iOrientation = 0
    var mOrientation = 90
    private var mediaFileName: String? = null
    private var timeInMilliseconds = 0L
    private  var startTime:Long = SystemClock.uptimeMillis()
    private  var updatedTime:Long = 0L
    private  var timeSwapBuff:Long = 0L
    var saveVideoTask: SaveVideoTask? = null
    var savePicTask: SavePicTask? = null
    private var mPhotoAngle = 90
    var flashType = 1
    private var runTimePermission: RunTimePermission? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (runTimePermission != null) {
            runTimePermission!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        runTimePermission = RunTimePermission(this)

        runTimePermission!!.requestPermission(arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), object : RunTimePermission.RunTimePermissionListener {
            override fun permissionGranted() {
                // First we need to check availability of play services
                initControls()

                identifyOrientationEvents()

                //create a folder
                createFolder()

                //capture image on callback
                //capture image on callback
                captureImageCallback()

                //
                if (camera != null) {
                    val info = CameraInfo()
                    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                        binding.ivCameraFlashOff.setVisibility(View.INVISIBLE)
                    }
                }
            }

            override fun permissionDenied() {
            }
        })
    }

    private fun initControls() {
        mediaRecorder = MediaRecorder()
        binding.txtCounter.visibility = View.GONE
        activeCameraCapture()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun activeCameraCapture() {
        if (binding.ivCaptureImage != null) {
            binding.ivCaptureImage.setAlpha(1.0f)

            binding.ivCaptureImage.setOnLongClickListener(OnLongClickListener {
                binding.txtVHintiew.setVisibility(View.INVISIBLE)
                try {
                    if (prepareMediaRecorder()) {
                        myOrientationEventListener!!.disable()
                        mediaRecorder!!.start()
                        startTime = SystemClock.uptimeMillis()
                        customHandler.postDelayed(updateTimerThread, 0)
                    } else {
                        return@OnLongClickListener false
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                binding.txtCounter.setVisibility(View.VISIBLE)
                binding.ivRotateCamera.setVisibility(View.GONE)
                binding.ivCameraFlashOff.setVisibility(View.INVISIBLE)
                scaleUpAnimation()

                binding.ivCaptureImage.setOnTouchListener(OnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_BUTTON_PRESS) {
                        return@OnTouchListener true
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        scaleDownAnimation()
                        binding.txtVHintiew.setVisibility(View.VISIBLE)
                        cancelSaveVideoTaskIfNeed()
                        saveVideoTask =
                            SaveVideoTask(this)
                        saveVideoTask!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
                        return@OnTouchListener true
                    }
                    true
                })
                true
            })

            binding.ivCaptureImage.setOnClickListener(View.OnClickListener {
                if (isSpaceAvailable()) {
                    captureImage()
                } else {
                    toast(
                        "Memory is not available"
                    )
                }
            })
        }
    }

    private fun scaleDownAnimation() {
        val scaleDownX = ObjectAnimator.ofFloat(binding.ivCaptureImage, "scaleX", 1f)
        val scaleDownY = ObjectAnimator.ofFloat(binding.ivCaptureImage, "scaleY", 1f)
        scaleDownX.duration = 100
        scaleDownY.duration = 100
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)

        scaleDownX.addUpdateListener {
            val p = binding.ivCaptureImage.getParent() as View
            p.invalidate()
        }
        scaleDown.start()
    }

    private fun scaleUpAnimation() {
        val scaleDownX = ObjectAnimator.ofFloat(binding.ivCaptureImage, "scaleX", 2f)
        val scaleDownY = ObjectAnimator.ofFloat(binding.ivCaptureImage, "scaleY", 2f)
        scaleDownX.duration = 100
        scaleDownY.duration = 100
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)

        scaleDownX.addUpdateListener {
            val p = binding.ivCaptureImage.getParent() as View
            p.invalidate()
        }
        scaleDown.start()
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    protected fun prepareMediaRecorder(): Boolean {
        mediaRecorder = MediaRecorder() // Works well
        camera!!.stopPreview()
        camera!!.unlock()
        mediaRecorder!!.setCamera(camera)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        if (flag == 1) {
            mediaRecorder!!.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_HIGH))
        } else {
            mediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
        }
        mediaRecorder!!.setPreviewDisplay(surfaceHolder!!.surface)
        mediaRecorder!!.setOrientationHint(mOrientation)
        if (Build.MODEL.equals("Nexus 6", ignoreCase = true) && flag == 1) {
            if (mOrientation == 90) {
                mediaRecorder!!.setOrientationHint(mOrientation)
            } else if (mOrientation == 180) {
                mediaRecorder!!.setOrientationHint(0)
            } else {
                mediaRecorder!!.setOrientationHint(180)
            }
        } else if (mOrientation == 90 && flag == 1) {
            mediaRecorder!!.setOrientationHint(270)
        } else if (flag == 1) {
            mediaRecorder!!.setOrientationHint(mOrientation)
        }
        mediaFileName = "smart_station_" + System.currentTimeMillis()
        mediaRecorder!!.setOutputFile(folderVideo!!.getAbsolutePath() + "/" + mediaFileName + ".mp4") // Environment.getExternalStorageDirectory()
        mediaRecorder!!.setOnInfoListener { mr, what, extra -> // TODO Auto-generated method stub
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                val downTime: Long = 0
                val eventTime: Long = 0
                val x = 0.0f
                val y = 0.0f
                val metaState = 0
                val motionEvent = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_UP, 0f, 0f,
                    metaState
                )
                binding.ivCaptureImage.dispatchTouchEvent(motionEvent)
                Toast.makeText(
                    this,
                    "You reached to Maximum(25MB) video size.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mediaRecorder!!.setMaxFileSize((1000 * 25 * 1000).toLong())
        try {
            mediaRecorder!!.prepare()
        } catch (e: java.lang.Exception) {
            releaseMediaRecorder()
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.reset() // clear recorder configuration
            mediaRecorder!!.release() // release the recorder object
            mediaRecorder = MediaRecorder()
        }
    }

    private fun identifyOrientationEvents() {
        myOrientationEventListener =
            object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(iAngle: Int) {
                    val iLookup = intArrayOf(
                        0,
                        0,
                        0,
                        90,
                        90,
                        90,
                        90,
                        90,
                        90,
                        180,
                        180,
                        180,
                        180,
                        180,
                        180,
                        270,
                        270,
                        270,
                        270,
                        270,
                        270,
                        0,
                        0,
                        0
                    ) // 15-degree increments
                    if (iAngle != ORIENTATION_UNKNOWN) {
                        val iNewOrientation = iLookup[iAngle / 15]
                        if (iOrientation != iNewOrientation) {
                            iOrientation = iNewOrientation
                            if (iOrientation == 0) {
                                mOrientation = 90
                            } else if (iOrientation == 270) {
                                mOrientation = 0
                            } else if (iOrientation == 90) {
                                mOrientation = 180
                            }
                        }
                        mPhotoAngle = normalize(iAngle)
                    }
                }
            }

        if (myOrientationEventListener!!.canDetectOrientation()) {
            myOrientationEventListener!!.enable()
        }
    }

    private fun createFolder() {
        folderImg = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "com.smartstation"+"/Smart Station/Media/Smart Station Images"
        )
        if (!folderImg!!.exists()) {
            folderImg!!.mkdirs()
        }

        folderVideo = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "com.smartstation"+"/Smart Station/Media/Smart Station Video"
        )
        if (!folderVideo!!.exists()) {
            folderVideo!!.mkdirs()
        }
    }


    private fun setOnClickListener() {

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivRotateCamera.setOnClickListener {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
            flag = if (flag == 0) {
                binding.ivCameraFlashOff.setVisibility(View.GONE)
                1
            } else {
                binding.ivCameraFlashOff.setVisibility(View.VISIBLE)
                0
            }
            surfaceCreated(surfaceHolder!!)
        }

        binding.ivCameraFlashOff.setOnClickListener {
            flashToggle()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            if (myOrientationEventListener != null) myOrientationEventListener!!.enable()
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime
            updatedTime = timeSwapBuff + timeInMilliseconds
            var secs: Int = (updatedTime / 1000).toInt()
            val mins = secs / 60
            val hrs = mins / 60
            secs = secs % 60
            binding.txtCounter.setText(String.format("%02d", mins) + ":" + String.format("%02d", secs))
            customHandler.postDelayed(this, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (customHandler != null) customHandler.removeCallbacksAndMessages(null)
            releaseMediaRecorder() // if you are using MediaRecorder, release it first
            if (myOrientationEventListener != null) myOrientationEventListener!!.enable()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelSavePicTaskIfNeed() {
        if (savePicTask != null && savePicTask!!.getStatus() == AsyncTask.Status.RUNNING) {
            savePicTask!!.cancel(true)
        }
    }

    private fun cancelSaveVideoTaskIfNeed() {
        if (saveVideoTask != null && saveVideoTask!!.getStatus() == AsyncTask.Status.RUNNING) {
            saveVideoTask!!.cancel(true)
        }
    }

    class SavePicTask(activity: CameraActivity,val data: ByteArray, rotation: Int) :
        AsyncTask<Void?, Void?, String?>() {
        var activity : CameraActivity? = null
        var rotation = 0



        override fun onPreExecute() {}
        override fun doInBackground(vararg p0: Void?): String? {
            return try {
                activity!!.saveToSDCard(data, rotation)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: String?) {
            activity!!.activeCameraCapture()
            activity!!.tempFile = File(result)
            Handler().postDelayed({
                val mIntent = Intent(
                    activity,
                    ImagePerviewActivity::class.java
                )
                mIntent.putExtra(Constants.FILE_PATH, activity!!.tempFile.toString())
                mIntent.putExtra(Constants.TYPE, "img")
                activity!!.startActivityForResult(mIntent, RESULT_CODE)
//                activity!!.finish()
            }, 100)
        }

        init {
            this.activity = activity
            this.rotation = rotation
        }
    }

    @Throws(IOException::class)
    fun saveToSDCard(data: ByteArray, rotation: Int): String? {
        var imagePath: String? = ""
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val reqHeight = metrics.heightPixels
            val reqWidth = metrics.widthPixels
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
            if (rotation != 0) {
                val mat = Matrix()
                mat.postRotate(rotation.toFloat())
                val bitmap1 =
                    Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, mat, true)
                if (bitmap != bitmap1) {
                    bitmap.recycle()
                }
                imagePath = getSavePhotoLocal(bitmap1)
                bitmap1?.recycle()
            } else {
                imagePath = getSavePhotoLocal(bitmap)
                bitmap?.recycle()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return imagePath
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            inSampleSize = if (width > height) {
                Math.round(height.toFloat() / reqHeight.toFloat())
            } else {
                Math.round(width.toFloat() / reqWidth.toFloat())
            }
        }
        return inSampleSize
    }

    private fun getSavePhotoLocal(bitmap: Bitmap?): String? {
        var path = ""
        try {
            val output: OutputStream
            val file: File =
                File(folderImg!!.getAbsolutePath(), "smart_station_" + System.currentTimeMillis() + ".jpg")
            try {
                output = FileOutputStream(file)
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.flush()
                output.close()
                path = file.absolutePath
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return path
    }

    fun captureImageCallback() {
        surfaceHolder = binding.imgSurface.holder
        surfaceHolder!!.addCallback(this)
        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        jpegCallback = Camera.PictureCallback { data, camera ->
            refreshCamera()
            cancelSavePicTaskIfNeed()
            savePicTask = SavePicTask(this@CameraActivity,data, getPhotoRotation())
            savePicTask!!.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR)
        }
    }

    class SaveVideoTask(activity: CameraActivity) :
        AsyncTask<Void?, Void?, Void?>() {
        @SuppressLint("StaticFieldLeak")
        var activity:CameraActivity? = null
        var thumbFilename: File? = null
        var progressDialog: ProgressDialog? = null

        init {
            this.activity = activity
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onPreExecute() {
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setMessage("Processing a video...")
            progressDialog!!.show()
            super.onPreExecute()
            activity!!.binding.ivCaptureImage.setOnTouchListener(null)
            activity!!.binding.txtCounter.setVisibility(View.GONE)
            activity!!.binding.ivRotateCamera.setVisibility(View.VISIBLE)
            activity!!.binding.ivCameraFlashOff.setVisibility(View.VISIBLE)
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (progressDialog != null) {
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                }
            }
            if (activity!!.tempFile != null && thumbFilename != null) activity!!.onVideoSendDialog(
                activity!!.tempFile!!.getAbsolutePath(),
                thumbFilename!!.absolutePath
            )
        }

        override fun doInBackground(vararg p0: Void?): Void? {
            try {
                try {
                    activity!!.myOrientationEventListener!!.enable()
                    activity!!.customHandler.removeCallbacksAndMessages(null)
                    activity!!.mediaRecorder!!.stop()
                    activity!!.releaseMediaRecorder()
                    val name = activity!!.mediaFileName
                    activity!!.tempFile = File(activity!!.folderVideo!!.getAbsolutePath() + "/" + name + ".mp4")
                    thumbFilename = File(activity!!.folderVideo!!.getAbsolutePath(), "$name.jpeg")
                    activity!!.generateVideoThmb(activity!!.tempFile!!.getPath(), thumbFilename)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun onVideoSendDialog(videopath: String?, thumbPath: String) {
        runOnUiThread {
            if (videopath != null) {
                val fileVideo = File(videopath)
                val fileSizeInBytes = fileVideo.length()
                val fileSizeInKB = fileSizeInBytes / 1024
                val fileSizeInMB = fileSizeInKB / 1024
                if (fileSizeInMB > MAX_VIDEO_SIZE_UPLOAD) {
                    AlertDialog.Builder(this)
                        .setMessage(getString(R.string.file_limit_size_upload_format,
                            MAX_VIDEO_SIZE_UPLOAD))
                        .setPositiveButton("OK"
                        ) { dialog, which -> dialog.dismiss() }
                        .show()
                } else {
                    val mIntent = Intent(this,
                        ImagePerviewActivity::class.java)
                    mIntent.putExtra(Constants.FILE_PATH, videopath.toString())
                    mIntent.putExtra(Constants.TYPE, "video")
                    startActivityForResult(mIntent, RESULT_CODE)
//                    finish()
                    //SendVideoDialog sendVideoDialog = SendVideoDialog.newInstance(videopath, thumbPath, name, phoneNuber);
                    // sendVideoDialog.show(getSupportFragmentManager(), "SendVideoDialog");
                }
            }
        }
    }

    private fun inActiveCameraCapture() {
        if (binding.ivCaptureImage != null) {
            binding.ivCaptureImage.setAlpha(0.5f)
            binding.ivCaptureImage.setOnClickListener(null)
        }
    }


    fun getFreeSpacePercantage(): Int {
        val percantage = (freeMemory() * 100 / totalMemory()).toInt()
        val modValue = percantage % 5
        return percantage - modValue
    }

    fun totalMemory(): Double {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val sdAvailSize = stat.blockCount.toDouble() * stat.blockSize.toDouble()
        return sdAvailSize / 1073741824
    }

    fun freeMemory(): Double {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val sdAvailSize = stat.availableBlocks.toDouble() * stat.blockSize.toDouble()
        return sdAvailSize / 1073741824
    }

    fun isSpaceAvailable(): Boolean {
        return if (getFreeSpacePercantage() >= 1) {
            true
        } else {
            false
        }
    }

    fun generateVideoThmb(srcFilePath: String?, destFile: File?) {
        try {
            val bitmap = ThumbnailUtils.createVideoThumbnail(srcFilePath!!, 120)
            val out = FileOutputStream(destFile)
            ThumbnailUtils.extractThumbnail(bitmap, 200, 200)
                .compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun normalize(degrees: Int): Int {
        if (degrees > 315 || degrees <= 45) {
            return 0
        }
        if (degrees > 45 && degrees <= 135) {
            return 90
        }
        if (degrees > 135 && degrees <= 225) {
            return 180
        }
        if (degrees > 225 && degrees <= 315) {
            return 270
        }
        throw RuntimeException("Error....")
    }

    private fun getPhotoRotation(): Int {
        val rotation: Int
        val orientation = mPhotoAngle
        val info = CameraInfo()
        if (flag == 0) {
            Camera.getCameraInfo(0, info)
        } else {
            Camera.getCameraInfo(1, info)
        }
        rotation = if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            (info.orientation - orientation + 360) % 360
        } else {
            (info.orientation + orientation) % 360
        }
        return rotation
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        camera = try {
            if (flag == 0) {
                Camera.open(0)
            } else {
                Camera.open(1)
            }
        } catch (e: java.lang.RuntimeException) {
            e.printStackTrace()
            return
        }
        try {
            val param: Camera.Parameters
            param = camera!!.getParameters()
            val sizes = param.supportedPreviewSizes
            //get diff to get perfact preview sizes
            val displaymetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displaymetrics)
            val height = displaymetrics.heightPixels
            val width = displaymetrics.widthPixels
            val diff = (height * 1000 / width).toLong()
            var cdistance = Int.MAX_VALUE.toLong()
            var idx = 0
            for (i in sizes.indices) {
                val value = (sizes[i].width * 1000).toLong() / sizes[i].height
                if (value > diff && value < cdistance) {
                    idx = i
                    cdistance = value
                }
            }
            val cs = sizes[idx]
            param.setPreviewSize(cs.width, cs.height)
//            param.setPictureSize(cs.width, cs.height)
            val focusModes: List<String> = param.getSupportedFocusModes()
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
            }
            camera!!.setParameters(param)
            setCameraDisplayOrientation(0)
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()

            if (flashType == 1) {
                param.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_auto)
            } else if (flashType == 2) {
                param.flashMode = Camera.Parameters.FLASH_MODE_ON
                var params: Camera.Parameters? = null
                if (camera != null) {
                    params = camera!!.getParameters()
                    if (params != null) {
                        val supportedFlashModes = params.supportedFlashModes
                        if (supportedFlashModes != null) {
                            if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                param.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                            } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                param.flashMode = Camera.Parameters.FLASH_MODE_ON
                            }
                        }
                    }
                }
                binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_on)
            } else if (flashType == 3) {
                param.flashMode = Camera.Parameters.FLASH_MODE_OFF
                binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_off)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        refreshCamera()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        try {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun refreshCamera() {
        if (surfaceHolder!!.surface == null) {
            return
        }
        try {
            camera!!.stopPreview()
            val param = camera!!.parameters
            if (flag == 0) {
                if (flashType == 1) {
                    param.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                    binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_auto)
                } else if (flashType == 2) {
                    param.flashMode = Camera.Parameters.FLASH_MODE_ON
                    var params: Camera.Parameters? = null
                    if (camera != null) {
                        params = camera!!.parameters
                        if (params != null) {
                            val supportedFlashModes = params.supportedFlashModes
                            if (supportedFlashModes != null) {
                                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                    param.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                                } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                                    param.flashMode = Camera.Parameters.FLASH_MODE_ON
                                }
                            }
                        }
                    }
                    binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_on)
                } else if (flashType == 3) {
                    param.flashMode = Camera.Parameters.FLASH_MODE_OFF
                    binding.ivCameraFlashOff.setImageResource(R.drawable.ic_flash_off)
                }
            }
            refrechCameraPriview(param)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun refrechCameraPriview(param: Camera.Parameters) {
        try {
            camera!!.parameters = param
            setCameraDisplayOrientation(0)
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun setCameraDisplayOrientation(cameraId: Int) {
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        var rotation = windowManager.defaultDisplay.rotation
        if (Build.MODEL.equals("Nexus 6", ignoreCase = true) && flag == 1) {
            rotation = Surface.ROTATION_180
        }
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera!!.setDisplayOrientation(result)
    }

    private fun flashToggle() {
        if (flashType == 1) {
            flashType = 2
        } else if (flashType == 2) {
            flashType = 3
        } else if (flashType == 3) {
            flashType = 1
        }
        refreshCamera()
    }

    private fun captureImage() {
        camera!!.takePicture(null, null, jpegCallback)
        inActiveCameraCapture()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        cancelSavePicTaskIfNeed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val value = data.getStringExtra("file_url")
            val intent = Intent()
            intent.putExtra("file_url", value)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}