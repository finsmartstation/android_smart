package com.application.smartstation.ui.activity

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityImagePerviewBinding
import com.application.smartstation.service.Status
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.SmartStationVideoView
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class ImagePerviewActivity : BaseActivity(), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
    SmartStationVideoView.MediaPlayerControl {

    val binding: ActivityImagePerviewBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var player: MediaPlayer? = null
    var controller: SmartStationVideoView? = null
    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_perview)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null) {
            val type = intent.getStringExtra(Constants.TYPE)
            path = intent.getStringExtra(Constants.FILE_PATH)!!
            Log.d("TAG", "initView: " + path)
            if (type.equals("img")) {
                binding.imgShow.visibility = View.VISIBLE
                Glide.with(this).load(path).diskCacheStrategy(
                    DiskCacheStrategy.DATA).into(binding.imgShow)
            } else {
                binding.videoSurfaceContainer.visibility = View.VISIBLE
                videoView(path)
            }
        }
    }

    private fun videoView(path: String?) {
        val videoHolder: SurfaceHolder = binding.videoSurface.holder
        videoHolder.addCallback(this)
        player = MediaPlayer()
        player!!.setOnPreparedListener(this)
        controller = SmartStationVideoView(this)

        try {
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player!!.setDataSource(this,
                Uri.parse(path))
            player!!.setOnPreparedListener(this)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        try {
            controller!!.show()
        } catch (e: Exception) {
            Log.d("TAG", "onTouchEvent: " + e)
        }
        return false
    }

    private fun setOnClickListener() {
        binding.imgBack.setOnClickListener {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            finish()
        }

        binding.llSend.setOnClickListener {
            if (path.isNotEmpty()) {
                val files = File(path)
                val requestBody =
                    files.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val file = MultipartBody.Part.createFormData("file", files.name, requestBody)
                val user_id: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val accessToken: RequestBody = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                fileUpload(user_id, accessToken, file)
            }
        }
    }

    private fun fileUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        file: MultipartBody.Part,
    ) {
        apiViewModel.fileUpload(user_id, accessToken, file).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            val intent = Intent()
                            intent.putExtra("file_url", it.data.filepath)
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            toast(it.data.message)
                        }
                    }
                    Status.ERROR -> {
                        dismissProgress()
                        toast(it.message!!)
                    }
                }
            }
        })
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        player!!.setDisplay(p0)
        player!!.prepareAsync()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    override fun onPrepared(p0: MediaPlayer?) {
        controller!!.setMediaPlayer(this)
        controller!!.setAnchorView(binding.videoSurfaceContainer)
        player!!.start()
    }

    override fun start() {
        player!!.start()
    }

    override fun pause() {
        player!!.pause()
    }

    override fun getDuration(): Int {
        return player!!.duration
    }

    override fun getCurrentPosition(): Int {
        return player!!.currentPosition
    }

    override fun seekTo(pos: Int) {
        player!!.seekTo(pos)
    }

    override fun isPlaying(): Boolean {
        return player!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun isFullScreen(): Boolean {
        return false
    }

    override fun toggleFullScreen() {
    }

}