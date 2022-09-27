package com.application.smartstation.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNotificationBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : BaseActivity() {

    val binding: ActivityNotificationBinding by viewBinding()
    var notifyStatus: Int = 1
    var sound: Int = 1
    var vibrate: Int = 1
    val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.notification)
        binding.ilHeader.imgHeader.visibility = View.GONE
        binding.ilHeader.imgAudio.visibility = View.GONE
        binding.ilHeader.imgVideo.visibility = View.GONE

        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        getNotification(inputParams)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.scNotification.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && binding.scNotification.isClickable) {
                notifyStatus = 1
            } else if (!isChecked && binding.scNotification.isClickable) {
                notifyStatus = 0
            }
        })

        binding.scSound.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && binding.scSound.isClickable) {
                sound = 1
            } else if (!isChecked && binding.scSound.isClickable) {
                sound = 0
            }
        })

        binding.scVibrate.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && binding.scVibrate.isClickable) {
                vibrate = 1
            } else if (!isChecked && binding.scVibrate.isClickable) {
                vibrate = 0
            }
        })

        binding.btnUpdate.setOnClickListener {
            val inputParams = InputParams()
            inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
            inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
            inputParams.notification_status = notifyStatus.toString()
            inputParams.sound = sound.toString()
            inputParams.vibration = vibrate.toString()
            setNotification(inputParams)
        }

    }

    fun setNotification(inputParams: InputParams) {
        apiViewModel.changeNotification(inputParams).observe(this, Observer {
            it?.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (it.data.notification_status.equals("1")) {
                                notifyStatus = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.NOTIFICATION_SHOW,
                                    true)
                                binding.scNotification.isChecked = true
                            } else {
                                notifyStatus = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.NOTIFICATION_SHOW,
                                    false)
                                binding.scNotification.isChecked = false
                            }

                            if (it.data.sound.equals("1")) {
                                sound = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.SOUND, true)
                                binding.scSound.isChecked = true
                            } else {
                                sound = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.SOUND, false)
                                binding.scSound.isChecked = false
                            }

                            if (it.data.vibration.equals("1")) {
                                vibrate = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.VIBRATE, true)
                                binding.scVibrate.isChecked = true
                            } else {
                                vibrate = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.VIBRATE, false)
                                binding.scVibrate.isChecked = false
                            }
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

    fun getNotification(inputParams: InputParams) {
        apiViewModel.getNotification(inputParams).observe(this, Observer {
            it?.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            if (it.data.notification_status.equals("1")) {
                                notifyStatus = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.NOTIFICATION_SHOW,
                                    true)
                                binding.scNotification.isChecked = true
                            } else {
                                notifyStatus = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.NOTIFICATION_SHOW,
                                    false)
                                binding.scNotification.isChecked = false
                            }

                            if (it.data.sound.equals("1")) {
                                sound = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.SOUND, true)
                                binding.scSound.isChecked = true
                            } else {
                                sound = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.SOUND, false)
                                binding.scSound.isChecked = false
                            }

                            if (it.data.vibration.equals("1")) {
                                vibrate = 1
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.VIBRATE, true)
                                binding.scVibrate.isChecked = true
                            } else {
                                vibrate = 0
                                UtilsDefault.updateSharedPreferenceBoolean(Constants.VIBRATE, false)
                                binding.scVibrate.isChecked = false
                            }
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
}