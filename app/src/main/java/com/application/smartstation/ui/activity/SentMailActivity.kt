package com.application.smartstation.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityMainBinding
import com.application.smartstation.databinding.ActivitySentMailBinding
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.adapter.InboxAdapter
import com.application.smartstation.ui.adapter.SentboxAdapter
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.application.smartstation.viewmodel.InboxEvent
import com.application.smartstation.viewmodel.SentboxEvent
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SentMailActivity : BaseActivity() {

    val binding: ActivitySentMailBinding by viewBinding()
    var list:ArrayList<SendMailListRes> = ArrayList()
    var sentboxAdapter: SentboxAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()
    val emitters: SocketService.Emitters = SocketService.Emitters(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sent_mail)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.sentbox)
        binding.rvSentBox.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        sentboxAdapter = SentboxAdapter(this)
        binding.rvSentBox.adapter = sentboxAdapter

        sentboxAdapter!!.onItemClick = { model ->
            val intent = Intent(this,ViewMailActivity::class.java)
            intent.putExtra("boxType",2)
            intent.putExtra("id",model.id)
            startActivity(intent)
        }

        getSentBox()
        emitSentBox()
    }

    private fun emitSentBox() {
        if (UtilsDefault.isOnline()) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("user_id", UtilsDefault.getSharedPreferenceString(Constants.USER_ID))
                jsonObject.put("accessToken", UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN))
                emitters.sendMailList(jsonObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getSentBox() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.sendMail(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.data
                            if(list.isNotEmpty()) {
                                setData(list)
                            }
                        }else{
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

    private fun setData(list: ArrayList<SendMailListRes>) {
        sentboxAdapter!!.setMail(list)
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSentboxEvent(event: SentboxEvent) {
        var jsonObject: JSONObject? = JSONObject()
        jsonObject = event.getJsonObject()
        setInboxEvent(jsonObject)
        Log.d("TAG", "ONCHATEVENT: "+jsonObject)
    }

    fun setInboxEvent(jsonObject: JSONObject?) {
        val gson = Gson()
        val mailSocketModel: SendMailRes = gson.fromJson(jsonObject.toString(),
            SendMailRes::class.java)
        if (mailSocketModel.status){
            if (mailSocketModel.data.isNotEmpty()){
                setData(mailSocketModel.data)
            }
        }else{
            toast(mailSocketModel.message)
        }
    }
}