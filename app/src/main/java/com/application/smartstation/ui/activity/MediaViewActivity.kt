package com.application.smartstation.ui.activity

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMediaViewBinding
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.view.SmartStationVideoView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import java.io.IOException

class MediaViewActivity : BaseActivity(), MediaPlayer.OnPreparedListener,
    SmartStationVideoView.MediaPlayerControl, SurfaceHolder.Callback{

    val binding: ActivityMediaViewBinding by viewBinding()
    var name = ""
    var msg = ""
    var date = ""
    var player: MediaPlayer? = null
    var controller: SmartStationVideoView? = null
    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_view)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if(intent != null){
            name = intent.getStringExtra("name")!!
            msg = intent.getStringExtra("msg")!!
            date = intent.getStringExtra("date")!!
            type = intent.getStringExtra("type")!!

            binding.txtName.text = name
            val s = UtilsDefault.localTimeConvert(date)
            binding.txtDate.text = UtilsDefault.dateConvert(s)+" "+ UtilsDefault.todayDate(s)

            //Video
            if (type == "video") {
                binding.videoSurfaceContainer.visibility = View.VISIBLE
                binding.photoView.visibility = View.GONE
                videoViewfun(msg)
            } else if (type == "image") {
                binding.photoView.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                Glide.with(this).load(msg).into(binding.photoView)
            }
        }

    }

    private fun videoViewfun(msg: String) {
        val videoHolder: SurfaceHolder = binding.videoSurface.holder
        videoHolder.addCallback(this)
        player = MediaPlayer()
        player!!.setOnPreparedListener(this)
        controller = SmartStationVideoView(this)

        try {
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player!!.setDataSource(this,
                Uri.parse(msg))
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

    private fun setOnClickListener() {
        binding.imgBack.setOnClickListener {
            finish()
        }
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

    override fun surfaceCreated(p0: SurfaceHolder) {
        player!!.setDisplay(p0)
        player!!.prepareAsync()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        try {
            controller!!.show()
        } catch (e: Exception) {
            Log.d("TAG", "onTouchEvent: " + e)
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player!!.stop()
            player!!.release()
        }
    }
}