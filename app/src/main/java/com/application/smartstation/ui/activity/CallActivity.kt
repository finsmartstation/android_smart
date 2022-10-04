package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.*
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityCallBinding
import com.application.smartstation.service.ApiRequest
import com.application.smartstation.service.Callback
import com.application.smartstation.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.twilio.video.*
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class CallActivity : BaseActivity() {

    val binding: ActivityCallBinding by viewBinding()

    companion object {
        val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1
        val TAG = "VideoActivity"
        var is_calling_activity_open = false
        var callReceive = "Call_Receive"
        var callSend = "Call_Send"
        var callRinging = "Call_Ringing"
        var callPick = "Call_Pick"
        var callNotPick = "Call_not_Pick"
        val LOCAL_AUDIO_TRACK_NAME = "mic"
        val LOCAL_VIDEO_TRACK_NAME = "camera"
        var identity: String? = null
    }

    var audioCodec: AudioCodec? = null
    var videoCodec: VideoCodec? = null
    var encodingParameters: EncodingParameters? = null
    var localAudioTrack: LocalAudioTrack? = null
    var localVideoTrack: LocalVideoTrack? = null
    var audioManager: AudioManager? = null
    var remoteParticipantIdentity: String? = null

    var previousAudioMode = 0
    var previousMicrophoneMute = false
    var localVideoView: VideoRenderer? = null
    var disconnectedFromOnDestroy = false
    var isSpeakerPhoneEnabled = false
    var enableAutomaticSubscription = false
    var callerId: String? = null
    var callerName: String? = null
    var callerImage: String? = null
    var callStatus: String? = null
    var callType: String? = null
    var roomName: String? = null
    var ringtoneSound: Ringtone? = null
    var caller_token = "null"
    var cameraCapturerCompat: CameraCapturerCompat? = null
    var accessToken: String? = null
    var rootRef: DatabaseReference? = null
    var room: Room? = null
    var localParticipant: LocalParticipant? = null
    var duration: Long = 0
    var minutes = 0
    var countDownTimer: CountDownTimer? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        setContentView(R.layout.activity_call)
        initView()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.switchCameraActionFab.setOnClickListener {
            if (cameraCapturerCompat != null) {
                val cameraSource = cameraCapturerCompat!!.cameraSource
                cameraCapturerCompat!!.switchCamera()
                if (binding.videoView.thumbnailVideoView.visibility == View.VISIBLE) {
                    binding.videoView.thumbnailVideoView.mirror = cameraSource == CameraCapturer.CameraSource.BACK_CAMERA
                } else {
                    binding.videoView.thumbnailVideoView.mirror = cameraSource == CameraCapturer.CameraSource.BACK_CAMERA
                }
            }
        }

        binding.localVideoActionFab.setOnClickListener {
            /*
                 * Enable/disable the local video track
                 */if (localVideoTrack != null) {
            val enable = !localVideoTrack!!.isEnabled
            localVideoTrack!!.enable(enable)
            val icon: Int
            if (enable) {
                icon = R.drawable.ic_videocam_white_24dp
                binding.switchCameraActionFab.show()
            } else {
                icon = R.drawable.ic_videocam_off_black_24dp
                binding.switchCameraActionFab.hide()
            }
            binding.localVideoActionFab.setImageDrawable(
                ContextCompat.getDrawable(this, icon))
        }
        }

        binding.muteActionFab.setOnClickListener {
            /*
                 * Enable/disable the local audio track. The results of this operation are
                 * signaled to other Participants in the same Room. When an audio track is
                 * disabled, the audio is muted.
                 */

            /*
                 * Enable/disable the local audio track. The results of this operation are
                 * signaled to other Participants in the same Room. When an audio track is
                 * disabled, the audio is muted.
                 */if (localAudioTrack != null) {
            val enable = !localAudioTrack!!.isEnabled
            localAudioTrack!!.enable(enable)
            val icon =
                if (enable) R.drawable.ic_mic_white_24dp else R.drawable.ic_mic_off_black_24dp
            binding.muteActionFab.setImageDrawable(ContextCompat.getDrawable(
                this, icon))
        }
        }

        binding.speakerActionFab.setOnClickListener {
            if (audioManager!!.isSpeakerphoneOn) {
                audioManager!!.isSpeakerphoneOn = false
                binding.speakerActionFab.setImageResource(R.drawable.ic_volume_mute_black_24dp)
                isSpeakerPhoneEnabled = false
            } else {
                audioManager!!.isSpeakerphoneOn = true
                binding.speakerActionFab.setImageResource(R.drawable.ic_volume_up_white_24dp)
                isSpeakerPhoneEnabled = true
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView() {

        identity = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)

        rootRef = FirebaseDatabase.getInstance().reference

        //ringtone
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtoneSound = RingtoneManager.getRingtone(applicationContext, ringtoneUri)

        //plusator
        binding.pulsator.start()

        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        val intent = intent
        onNewIntent(intent)

        createAudioAndVideoTracks()
        retrieveAccessTokenfromServer()

        intializeUI()

        getUserToken()


    }

    private fun getUserToken() {
        rootRef!!.child("Users")
            .child(callerId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.hasChild("token")) {
                        caller_token = dataSnapshot.child("token").value.toString()
                    }
                    showCallingDialog()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun showCallingDialog() {
        binding.callingUserInfoLayout.visibility = View.VISIBLE
        binding.callingActionButtons.visibility = View.GONE
        binding.username.text = callerName

        binding.pickIncomingCall.setOnClickListener {
            if (accessToken != null) {
                if (callType == "video_call") {
                    binding.callingUserInfoLayout.visibility = View.GONE
                } else {
                    binding.switchCameraActionFab.visibility = View.GONE
                    binding.localVideoActionFab.visibility = View.GONE
                    binding.receiveDisconnectLayout.visibility = View.GONE
                    binding.cancelCall.visibility = View.GONE
                }
                binding.callingActionButtons.visibility = View.VISIBLE
                if (ringtoneSound != null) {
                    ringtoneSound!!.stop()
                }
                connectToRoom(roomName)
                Send_notification(callPick, "Pick your Call")
            } else {
                Toast.makeText(this,
                    "Call Access token not Found",
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelIncomingButton.setOnClickListener {
            Send_notification(callNotPick, "Hang out your call")
            if (ringtoneSound != null) {
                ringtoneSound!!.stop()
            }
            finish()
        }

        binding.cancelCall.visibility = View.VISIBLE
        binding.cancelCall.setOnClickListener {
            Send_notification(callNotPick, "Hang out your call")
            disconnectClickListener()
            finish()
        }

        if (callStatus == callSend) {
            binding.receiveDisconnectLayout.visibility = View.GONE
            binding.cancelCall.visibility = View.VISIBLE
            Send_notification(callReceive, "Calling you...")
            Start_Countdown_timer()
        } else if (callStatus == callReceive) {
            binding.receiveDisconnectLayout.visibility = View.VISIBLE
            binding.cancelCall.visibility = View.GONE
            binding.callingStatusText.text = "Calling you..."
            Send_notification(callRinging, "Ringing...")
        }

    }

    fun Start_Countdown_timer() {
        countDownTimer = object : CountDownTimer(60000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Send_notification(callNotPick,
                    "Calling you...")
                disconnectClickListener()
                finish()
            }
        }
        countDownTimer!!.start()
    }

    fun Stop_timer() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
    }

    private fun disconnectClickListener() {
        if (room != null) {
            room!!.disconnect()
            if (callType == "video_call") {
                minutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
            } else {
                minutes = Math.round(TimeUnit.MILLISECONDS.toMinutes(duration).toFloat())
            }
            minutes = minutes + 1

        }
        this@CallActivity.intializeUI()
        finish()
    }

    private fun connectToRoom(roomName: String?) {
        configureAudio(true)
        val connectOptionsBuilder = ConnectOptions.Builder(accessToken!!)
            .roomName(roomName!!)


        if (localAudioTrack != null) {
            connectOptionsBuilder
                .audioTracks(listOf(localAudioTrack!!))
        }


        if (localVideoTrack != null && callType == "video_call") {
            connectOptionsBuilder.videoTracks(listOf(localVideoTrack!!))
        }


        connectOptionsBuilder.preferAudioCodecs(listOf(audioCodec))

        if (callType == "video_call") connectOptionsBuilder.preferVideoCodecs(listOf(
            videoCodec))


        connectOptionsBuilder.encodingParameters(encodingParameters!!)
        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription)

        room = Video.connect(this, connectOptionsBuilder.build(), roomListener()!!)
        setDisconnectAction()
    }

    fun setDisconnectAction() {
        binding.connectActionFab.show()
        binding.connectActionFab.setOnClickListener {
            disconnectClickListener()
        }
    }

    private fun roomListener(): Room.Listener? {
        return object : Room.Listener {
            override fun onConnected(room: Room) {
                localParticipant = room.localParticipant
                Log.d(TAG, "onConnected: " + room.remoteParticipants.count())
//                binding.videoView.videoStatusTextView.setText(getString(R.string.connected_to) + " " + callerName)
                binding.callingStatusText.text = getString(R.string.connected)
                title = room.name
//                Start_limit_Timer()
                for (remoteParticipant in room.remoteParticipants) {
                    addRemoteParticipant(remoteParticipant)
                    break
                }
            }

            override fun onReconnecting(room: Room, twilioException: TwilioException) {
//                binding.videoView.videoStatusTextView.setText(getString(R.string.reconnecting_to) + callerName)
                binding.callingStatusText.setText(R.string.reconnecting)
                binding.videoView.reconnectingProgressBar.visibility = View.VISIBLE
            }

            override fun onReconnected(room: Room) {
//                binding.videoView.videoStatusTextView.setText(getString(R.string.connected_to) + room.name)
                binding.callingStatusText.setText(R.string.connected)
                binding.videoView.reconnectingProgressBar.visibility = View.GONE
            }

            override fun onConnectFailure(room: Room, e: TwilioException) {
//                binding.videoView.videoStatusTextView.setText(R.string.failed_to_connect)
                binding.callingStatusText.setText(R.string.disconnected)
                configureAudio(false)
                intializeUI()
            }

            override fun onDisconnected(room: Room, e: TwilioException?) {
                localParticipant = null
//                binding.videoView.videoStatusTextView.setText(getString(R.string.disconnected_from) + callerName)
                binding.videoView.reconnectingProgressBar.visibility = View.GONE
                this@CallActivity.room = null

                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    configureAudio(false)
                    intializeUI()
                    moveLocalVideoToPrimaryView()
                }
//                Stop_limit_timer()
            }

            override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
                addRemoteParticipant(remoteParticipant)
            }

            override fun onParticipantDisconnected(
                room: Room,
                remoteParticipant: RemoteParticipant,
            ) {
                removeRemoteParticipant(remoteParticipant)
            }

            override fun onRecordingStarted(room: Room) {
                Log.d(TAG,
                    "onRecordingStarted")
            }

            override fun onRecordingStopped(room: Room) {
                Log.d(TAG,
                    "onRecordingStopped")
            }
        }
    }

    private fun removeRemoteParticipant(remoteParticipant: RemoteParticipant) {
//        binding.videoView.videoStatusTextView.setText("$callerName left.")
        if (remoteParticipant.identity != remoteParticipantIdentity) {
            return
        }

        /*
         * Remove remote participant renderer
         */if (!remoteParticipant.remoteVideoTracks.isEmpty()) {
            val remoteVideoTrackPublication = remoteParticipant.remoteVideoTracks[0]

            /*
             * Remove video only if subscribed to participant track
             */if (remoteVideoTrackPublication.isTrackSubscribed) {
                removeParticipantVideo(remoteVideoTrackPublication.remoteVideoTrack)
            }
        }
        moveLocalVideoToPrimaryView()
        if (remoteParticipant.identity.contains(callerId!!)) {
            Log.d("tag", "remoteParticipant.getIdentity().contains(callerId)")
            Toast.makeText(this, "$callerName hang out the call", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun removeParticipantVideo(videoTrack: VideoTrack?) {
        videoTrack!!.removeRenderer(binding.videoView.primaryVideoView)
    }

    private fun moveLocalVideoToPrimaryView() {
        if (binding.videoView.thumbnailVideoView.visibility == View.VISIBLE) {
            binding.videoView.thumbnailVideoView.visibility = View.GONE
            if (localVideoTrack != null) {
                localVideoTrack!!.removeRenderer(binding.videoView.thumbnailVideoView)
                localVideoTrack!!.addRenderer(binding.videoView.primaryVideoView)
            }
            localVideoView = binding.videoView.primaryVideoView
            binding.videoView.primaryVideoView.mirror = cameraCapturerCompat!!.cameraSource ===
                    CameraCapturer.CameraSource.FRONT_CAMERA
        }
    }

    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (binding.videoView.thumbnailVideoView.visibility == View.VISIBLE) {
            Snackbar.make(binding.connectActionFab,
                "Multiple participants are not currently support in this UI",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return
        }
        remoteParticipantIdentity = remoteParticipant.identity
//        binding.videoView.videoStatusTextView.setText(callerName + " " + getString(R.string.joined))

        /*
         * Add remote participant renderer
         */if (remoteParticipant.remoteVideoTracks.size > 0) {
            val remoteVideoTrackPublication = remoteParticipant.remoteVideoTracks[0]

            /*
             * Only render video tracks that are subscribed to
             */if (remoteVideoTrackPublication.isTrackSubscribed) {
                addRemoteParticipantVideo(remoteVideoTrackPublication.remoteVideoTrack!!)
            }
        }

        /*
         * Start listening for participant events
         */remoteParticipant.setListener(remoteParticipantListener())
    }

    private fun addRemoteParticipantVideo(videoTrack: VideoTrack) {
        moveLocalVideoToThumbnailView()
        binding.videoView.primaryVideoView.mirror = false
        videoTrack.addRenderer(binding.videoView.primaryVideoView)
    }

    private fun moveLocalVideoToThumbnailView() {
        if (binding.videoView.thumbnailVideoView.visibility == View.GONE) {
            binding.videoView.thumbnailVideoView.visibility = View.VISIBLE
            localVideoTrack!!.removeRenderer(binding.videoView.primaryVideoView)
            localVideoTrack!!.addRenderer(binding.videoView.thumbnailVideoView)
            localVideoView = binding.videoView.thumbnailVideoView
            binding.videoView.thumbnailVideoView.mirror = cameraCapturerCompat!!.cameraSource ===
                    CameraCapturer.CameraSource.FRONT_CAMERA
        }
    }

    private fun configureAudio(enable: Boolean) {
        if (enable) {
            previousAudioMode = audioManager!!.mode
            // Request audio focus before making any device switch
            requestAudioFocus()
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
            /*
             * Always disable microphone mute during a WebRTC call.
             */previousMicrophoneMute = audioManager!!.isMicrophoneMute
            audioManager!!.isMicrophoneMute = false
        } else {
            audioManager!!.mode = previousAudioMode
            audioManager!!.abandonAudioFocus(null)
            audioManager!!.isMicrophoneMute = previousMicrophoneMute
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { }
                .build()
            audioManager!!.requestAudioFocus(focusRequest)
        } else {
            audioManager!!.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }

    private fun intializeUI() {
        binding.connectActionFab.show()
        binding.switchCameraActionFab.show()
        binding.localVideoActionFab.show()
        binding.muteActionFab.show()
        binding.speakerActionFab.show()
    }

    private fun retrieveAccessTokenfromServer() {
        val postRequest = StringRequest(Request.Method.GET,
            Constants.TWILIO_ACCESS_TOKEN_SERVER + "?identity=" + identity + "?roomname=" + roomName,
            object : Response.Listener<String?> {
                override fun onResponse(response: String?) {
                    Log.d("resp", response!!)
                    try {
                        val jsonObject = JSONObject(response)
                        val token = jsonObject.optString("token")
                        accessToken = token
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {}
            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        postRequest.retryPolicy = DefaultRetryPolicy(30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.cache.clear()
        requestQueue.add(postRequest)
    }

    private fun createAudioAndVideoTracks() {
        try {
            localAudioTrack = LocalAudioTrack.create(this,
                true,
                LOCAL_AUDIO_TRACK_NAME)
            if (callType == "video_call") {
                cameraCapturerCompat = CameraCapturerCompat(this, getAvailableCameraSource())
                localVideoTrack = LocalVideoTrack.create(this,
                    true,
                    cameraCapturerCompat!!.videoCapturer,
                    LOCAL_VIDEO_TRACK_NAME)
                binding.videoView.primaryVideoView.mirror = true
                if (localVideoTrack != null) {
                    localVideoTrack!!.addRenderer(binding.videoView.primaryVideoView)
                    localVideoView = binding.videoView.primaryVideoView
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAvailableCameraSource(): CameraCapturer.CameraSource? {
        return if (CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.FRONT_CAMERA)) CameraCapturer.CameraSource.FRONT_CAMERA else CameraCapturer.CameraSource.BACK_CAMERA
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            callerId = intent.getStringExtra(Constants.REC_ID)
            callerName = intent.getStringExtra(Constants.REC_NAME)
            callerImage = intent.getStringExtra(Constants.REC_PROFILE)
            callStatus = intent.getStringExtra(Constants.STATUS)
            callType = intent.getStringExtra(Constants.CALL_TYPE)
            roomName = intent.getStringExtra(Constants.ROOM_NAME)

        }

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (callType.equals("video_call")) {
            isSpeakerPhoneEnabled = true
            audioManager!!.isSpeakerphoneOn = isSpeakerPhoneEnabled
        } else {
            audioManager!!.isSpeakerphoneOn = isSpeakerPhoneEnabled
        }

        if (!callerImage.isNullOrEmpty()) {
            Glide.with(this).load(callerImage).placeholder(R.drawable.ic_default)
                .error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA)
                .into(binding.userimage)
        }
        do_action_on_status()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("RestrictedApi")
    fun do_action_on_status() {
        if (callStatus == callPick) {
            if (accessToken != null && binding.callingUserInfoLayout != null) {
                Stop_timer()
                if (callType == "video_call") binding.callingUserInfoLayout.visibility = View.GONE else {
                    binding.switchCameraActionFab.visibility = View.GONE
                    binding.localVideoActionFab.visibility = View.GONE
                    binding.receiveDisconnectLayout.visibility = View.GONE
                    binding.cancelCall.visibility = View.GONE
                    binding.username.setTextColor(getColor(R.color.white))
                }
                binding.callingActionButtons.visibility = View.VISIBLE
                connectToRoom(roomName)
                Toast.makeText(this, "Pick call", Toast.LENGTH_SHORT).show()
            }
        } else if (callStatus == callReceive) {
            if (ringtoneSound != null) {
                ringtoneSound!!.play()
            }
        } else if (callStatus == callRinging) {
            if (binding.callingStatusText != null) {
                binding.callingStatusText.setText(R.string.ringing)
            }
        } else if (callStatus == callNotPick) {
            if (ringtoneSound != null) {
                ringtoneSound!!.stop()
            }
            Toast.makeText(this, "$callerName hang out the call", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun Send_notification(call_status: String?, message: String?) {

        val name = UtilsDefault.getSharedPreferenceString(Constants.NAME)
        val image = UtilsDefault.getSharedPreferenceString(Constants.PROFILE_PIC)

        val json = JSONObject()
        try {
            json.put("to", caller_token)
            val info = JSONObject()
            info.put("senderId", identity)
            info.put("senderImage", image)
            info.put("title", name)
            info.put("body", message)
            info.put("type", callType)
            info.put("action", call_status)
            info.put("message", roomName)
            val ttl = JSONObject()
            ttl.put("ttl", "5s")
            json.put("notification", info)
            json.put("data", info)
            json.put("android", ttl)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        ApiRequest.sendNotification(this, json, object : Callback {
            override fun response(resp: String?) {}
        })
    }


    private fun remoteParticipantListener(): RemoteParticipant.Listener? {
        return object : RemoteParticipant.Listener {
            override fun onAudioTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(
                        "onAudioTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.identity,
                        remoteAudioTrackPublication.trackSid,
                        remoteAudioTrackPublication.isTrackEnabled,
                        remoteAudioTrackPublication.isTrackSubscribed,
                        remoteAudioTrackPublication.trackName))
//                binding.videoView.videoStatusTextView.setText(R.string.audio_connected)
            }

            override fun onAudioTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(("onAudioTrackUnpublished: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                            "subscribed=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteAudioTrackPublication.trackSid,
                        remoteAudioTrackPublication.isTrackEnabled,
                        remoteAudioTrackPublication.isTrackSubscribed,
                        remoteAudioTrackPublication.trackName))
//                binding.videoView.videoStatusTextView.setText(R.string.audio_unpublished)
            }

            override fun onDataTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(("onDataTrackPublished: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                            "subscribed=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteDataTrackPublication.trackSid,
                        remoteDataTrackPublication.isTrackEnabled,
                        remoteDataTrackPublication.isTrackSubscribed,
                        remoteDataTrackPublication.trackName))
            }

            override fun onDataTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(("onDataTrackUnpublished: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                            "subscribed=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteDataTrackPublication.trackSid,
                        remoteDataTrackPublication.isTrackEnabled,
                        remoteDataTrackPublication.isTrackSubscribed,
                        remoteDataTrackPublication.trackName))
            }

            override fun onVideoTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(("onVideoTrackPublished: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                            "subscribed=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteVideoTrackPublication.trackSid,
                        remoteVideoTrackPublication.isTrackEnabled,
                        remoteVideoTrackPublication.isTrackSubscribed,
                        remoteVideoTrackPublication.trackName))
//                binding.videoView.videoStatusTextView.setText(R.string.video_connected)
            }

            override fun onVideoTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
            ) {
                Log.i(TAG,
                    String.format(("onVideoTrackUnpublished: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                            "subscribed=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteVideoTrackPublication.trackSid,
                        remoteVideoTrackPublication.isTrackEnabled,
                        remoteVideoTrackPublication.isTrackSubscribed,
                        remoteVideoTrackPublication.trackName))
//                binding.videoView.videoStatusTextView.setText(R.string.video_unpublished)
            }

            override fun onAudioTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack,
            ) {
                Log.i(TAG,
                    String.format(("onAudioTrackSubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteAudioTrack.isEnabled,
                        remoteAudioTrack.isPlaybackEnabled,
                        remoteAudioTrack.name))
//                binding.videoView.videoStatusTextView.setText(R.string.audio_subscribed)
            }

            override fun onAudioTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack,
            ) {
                Log.i(TAG,
                    String.format(("onAudioTrackUnsubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteAudioTrack.isEnabled,
                        remoteAudioTrack.isPlaybackEnabled,
                        remoteAudioTrack.name))
//                binding.videoView.videoStatusTextView.setText(R.string.audio_unsubscribed)
            }

            override fun onAudioTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                twilioException: TwilioException,
            ) {
                Log.i(TAG,
                    String.format(("onAudioTrackSubscriptionFailed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteAudioTrackPublication: sid=%b, name=%s]" +
                            "[TwilioException: code=%d, message=%s]"),
                        remoteParticipant.identity,
                        remoteAudioTrackPublication.trackSid,
                        remoteAudioTrackPublication.trackName,
                        twilioException.code,
                        twilioException.message))
//                binding.videoView.videoStatusTextView.setText(R.string.audio_subscription_failed)
            }

            override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack,
            ) {
                Log.i(TAG,
                    String.format(("onDataTrackSubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteDataTrack: enabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteDataTrack.isEnabled,
                        remoteDataTrack.name))
            }

            override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack,
            ) {
                Log.i(TAG,
                    String.format(("onDataTrackUnsubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteDataTrack: enabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteDataTrack.isEnabled,
                        remoteDataTrack.name))
            }

            override fun onDataTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                twilioException: TwilioException,
            ) {
                Log.i(TAG,
                    String.format(("onDataTrackSubscriptionFailed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteDataTrackPublication: sid=%b, name=%s]" +
                            "[TwilioException: code=%d, message=%s]"),
                        remoteParticipant.identity,
                        remoteDataTrackPublication.trackSid,
                        remoteDataTrackPublication.trackName,
                        twilioException.code,
                        twilioException.message))
            }

            override fun onVideoTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack,
            ) {
                Log.i(TAG,
                    String.format(("onVideoTrackSubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteVideoTrack: enabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteVideoTrack.isEnabled,
                        remoteVideoTrack.name))
//                binding.videoView.videoStatusTextView.setText(R.string.video_subscribed)
                addRemoteParticipantVideo(remoteVideoTrack)
            }

            override fun onVideoTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack,
            ) {
                Log.i(TAG,
                    String.format(("onVideoTrackUnsubscribed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteVideoTrack: enabled=%b, name=%s]"),
                        remoteParticipant.identity,
                        remoteVideoTrack.isEnabled,
                        remoteVideoTrack.name))
//                binding.videoView.videoStatusTextView.setText(R.string.video_unsubscribed)
                removeParticipantVideo(remoteVideoTrack)
            }

            override fun onVideoTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                twilioException: TwilioException,
            ) {
                Log.i(TAG,
                    String.format(("onVideoTrackSubscriptionFailed: " +
                            "[RemoteParticipant: identity=%s], " +
                            "[RemoteVideoTrackPublication: sid=%b, name=%s]" +
                            "[TwilioException: code=%d, message=%s]"),
                        remoteParticipant.identity,
                        remoteVideoTrackPublication.trackSid,
                        remoteVideoTrackPublication.trackName,
                        twilioException.code,
                        twilioException.message))
//                binding.videoView.videoStatusTextView.setText(R.string.video_subscription_failed)
                Snackbar.make(binding.connectActionFab,
                    String.format("Failed to subscribe to %s video track",
                        remoteParticipant.identity),
                    Snackbar.LENGTH_LONG)
                    .show()
            }

            override fun onAudioTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
            ) {
            }

            override fun onAudioTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
            ) {
            }

            override fun onVideoTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
            ) {
            }

            override fun onVideoTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
            ) {
            }
        }
    }


    override fun onStart() {
        is_calling_activity_open = true
        audioCodec = getAudioCodecPreference(TwilioSettings.PREF_AUDIO_CODEC,
            TwilioSettings.PREF_AUDIO_CODEC_DEFAULT)
        videoCodec = getVideoCodecPreference(TwilioSettings.PREF_VIDEO_CODEC,
            TwilioSettings.PREF_VIDEO_CODEC_DEFAULT)
        enableAutomaticSubscription =
            getAutomaticSubscriptionPreference(TwilioSettings.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                TwilioSettings.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT)
        val newEncodingParameters: EncodingParameters = getEncodingParameters()!!
        if (localVideoTrack == null && callType == "video_call") {
            if (cameraCapturerCompat == null) cameraCapturerCompat =
                CameraCapturerCompat(this, getAvailableCameraSource())
            localVideoTrack = LocalVideoTrack.create(this,
                true,
                cameraCapturerCompat!!.videoCapturer,
                LOCAL_VIDEO_TRACK_NAME)
            localVideoTrack!!.addRenderer(localVideoView!!)
            if (localParticipant != null) {
                localParticipant!!.publishTrack(localVideoTrack!!)
                if (newEncodingParameters != encodingParameters) {
                    localParticipant!!.setEncodingParameters(newEncodingParameters)
                }
            }
        }
        encodingParameters = newEncodingParameters
        audioManager!!.isSpeakerphoneOn = isSpeakerPhoneEnabled
        if (room != null) {
            binding.videoView.reconnectingProgressBar.visibility = if (room!!.state != Room.State.RECONNECTING) View.GONE else View.VISIBLE
//            binding.videoView.videoStatusTextView.setText(getString(R.string.connected_to) + " " + callerName)
        }
        super.onStart()
    }

    @JvmName("getEncodingParameters1")
    private fun getEncodingParameters(): EncodingParameters? {
        val maxAudioBitrate: Int = TwilioSettings.PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT.toInt()
        val maxVideoBitrate: Int = TwilioSettings.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT.toInt()
        return EncodingParameters(maxAudioBitrate, maxVideoBitrate)
    }

    fun getAudioCodecPreference(key: String, defaultValue: String): AudioCodec? {
        val audioCodecName: String = defaultValue
        return when (audioCodecName) {
            IsacCodec.NAME -> IsacCodec()
            OpusCodec.NAME -> OpusCodec()
            PcmaCodec.NAME -> PcmaCodec()
            PcmuCodec.NAME -> PcmuCodec()
            G722Codec.NAME -> G722Codec()
            else -> OpusCodec()
        }
    }

    private fun getVideoCodecPreference(key: String, defaultValue: String): VideoCodec? {
        val videoCodecName: String = defaultValue
        return when (videoCodecName) {
            Vp8Codec.NAME -> {
                val simulcast: Boolean = TwilioSettings.PREF_VP8_SIMULCAST_DEFAULT
                Vp8Codec(simulcast)
            }
            H264Codec.NAME -> H264Codec()
            Vp9Codec.NAME -> Vp9Codec()
            else -> Vp8Codec()
        }
    }

    private fun getAutomaticSubscriptionPreference(key: String, defaultValue: Boolean): Boolean {
        return defaultValue
    }

    override fun onStop() {
        if (localVideoTrack != null) {
            if (localParticipant != null) {
                localParticipant!!.unpublishTrack(localVideoTrack!!)
            }
            localVideoTrack!!.release()
            localVideoTrack = null
        }
        super.onStop()
    }


    override fun onDestroy() {
        is_calling_activity_open = false
        configureAudio(false)
        if (ringtoneSound != null && ringtoneSound!!.isPlaying) {
            ringtoneSound!!.stop()
        }
        if (room != null && room!!.state != Room.State.DISCONNECTED) {
            room!!.disconnect()
            disconnectedFromOnDestroy = true
        }
        if (localAudioTrack != null) {
            localAudioTrack!!.release()
            localAudioTrack = null
        }
        if (localVideoTrack != null) {
            localVideoTrack!!.release()
            localVideoTrack = null
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            var cameraAndMicPermissionGranted = true
            for (grantResult in grantResults) {
                cameraAndMicPermissionGranted =
                    cameraAndMicPermissionGranted and (grantResult == PackageManager.PERMISSION_GRANTED)
            }
            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks()
                retrieveAccessTokenfromServer()
            } else {
                Toast.makeText(this,
                    "Need mic and speaker permission",
                    Toast.LENGTH_LONG).show()
            }
        }
    }


}