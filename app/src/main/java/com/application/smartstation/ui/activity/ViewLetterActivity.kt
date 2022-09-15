package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityViewLetterBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.ui.model.DataSentLetter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewLetterActivity : BaseActivity() {

    val binding: ActivityViewLetterBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var type = 0
    var sub = ""
    var from = ""
    var date = ""
    var body = ""
    var id = ""
    var path = ""
    var profile_pic = ""
    var to:Array<String>? = null
    var to1:Array<String>? = null
    var cc:Array<String>? = null
    var bcc:Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_letter)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null){
            type = intent.getIntExtra("boxType",0)
            id = intent.getStringExtra("id")!!
        }

        if (type.equals(1)){
            binding.ilHeader.txtHeader.text = resources.getString(R.string.inbox)
        }else{
            binding.ilHeader.txtHeader.text = resources.getString(R.string.sentbox)
        }
        
        viewLetterDetails(id)
    }

    private fun viewLetterDetails(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.viewLetter(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            setData(it.data.data)
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

    private fun setData(data: DataSentLetter) {
        from = data.from
        to = data.to
        sub = data.subject
        cc = data.cc
        bcc = data.bcc
        body = data.body
        date = data.datetime
        profile_pic = data.profile_pic

        if (!data.letter_path.isNullOrEmpty()){
            binding.clPdf.visibility = View.VISIBLE
            path = data.letter_path
            val fileName = FileUtils.getFileNameFromPath(data.letter_path).replace("/","")
            binding.txtFileName.text = fileName
        }

        if (!profile_pic.isNullOrEmpty()){
            Glide.with(this).load(profile_pic).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA).into(binding.imgProfile)
        }

        setValue()

    }

    private fun setValue() {
        binding.txtSub.text = sub
        binding.txtFrom.text = from

        binding.txtBody.text = Html.fromHtml(body)
        binding.txtDate.text = UtilsDefault.viewTime(date)

        for (i in 0 until to!!.size){
            if (i.equals(0)){
                if (to!!.size > 1) {
                    if (cc!!.size != 0){
                        if (bcc!!.size !=0){
                            binding.txtTo.text = to!![i] + " & " + ((to!!.size+cc!!.size+bcc!!.size)-1) + " more"
                        }else{
                            binding.txtTo.text = to!![i] + " & " + ((to!!.size+cc!!.size)-1) + " more"
                        }
                    }else if (bcc!!.size != 0){
                        binding.txtTo.text = to!![i] + " & " + ((to!!.size+bcc!!.size)-1) + " more"
                    }else {
                        binding.txtTo.text = to!![i] + " & " + (to!!.size - 1) + " more"
                    }
                }else{
                    if (cc!!.size != 0){
                        if (bcc!!.size !=0){
                            binding.txtTo.text = to!![i] + " & " + ((to!!.size+cc!!.size+bcc!!.size)-1) + " more"
                        }else{
                            binding.txtTo.text = to!![i] + " & " + ((to!!.size+cc!!.size)-1) + " more"
                        }
                    }else if (bcc!!.size != 0){
                        binding.txtTo.text = to!![i] + " & " + ((to!!.size+bcc!!.size)-1) + " more"
                    }else {
                        binding.txtTo.text = to!![i]
                    }
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.btnForward.setOnClickListener {
            startActivity(Intent(this,ForwardLetterActivity::class.java)
                .putExtra("sub",subjectFwdVal(sub))
                .putExtra("attach",path)
                .putExtra("id",id))
        }


        binding.btnForwardMail.setOnClickListener {
            UtilsDefault.downloadFile(this, path,object : MailCallback {
                override fun success(resp: String?, status: Boolean?) {
                    if (status!!){
                        startActivity(Intent(this@ViewLetterActivity,NewMailActivity::class.java)
                            .putExtra("sub",subjectFwdVal(sub))
                            .putExtra("attach",resp))
                    }
                }

            })

        }

        binding.txtTo.setOnClickListener {
            binding.txtTo.visibility = View.GONE
            binding.txtToFull.visibility = View.VISIBLE

            val toVal: String = TextUtils.join(",", to!!)
            val ccVal: String = TextUtils.join(",", cc!!)
            val bccVal: String = TextUtils.join(",", bcc!!)
            binding.txtToFull.text =toVal

            if (cc!!.size != 0) {
                if (bcc!!.size != 0) {
                    binding.txtCC.visibility = View.VISIBLE
                    binding.txtBCC.visibility = View.VISIBLE
                    binding.txtCC.text =ccVal
                    binding.txtBCC.text =bccVal
                }else{
                    binding.txtCC.visibility = View.VISIBLE
                    binding.txtCC.text =ccVal
                }
            }else{
                if (bcc!!.size != 0) {
                    binding.txtBCC.visibility = View.VISIBLE
                    binding.txtBCC.text =bccVal
                }
            }

        }

        binding.ilHeader.imgDelete.setOnClickListener {
            deleteLetter()
        }


        binding.clPdf.setOnClickListener {
            UtilsDefault.downloadFile(this, path,object : MailCallback {
                override fun success(resp: String?, status: Boolean?) {
                    if (status!!){
                        if (resp!!.contains(".pdf")){
                            startActivity(Intent(this@ViewLetterActivity,PdfViewActivity::class.java).putExtra("path",resp))
                        }else {
                            FileUtils.openDocument(this@ViewLetterActivity, resp)
                        }
                    }
                }

            })
        }
    }

    private fun subjectFwdVal(sub: String): String {
        if (sub.startsWith("Fwd:")){
            return sub
        }else if (sub.startsWith("Re:")){
            return sub.replace("Re:","Fwd:")
        }
        return "Fwd: "+sub
    }

    private fun deleteLetter() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.deleteLetter(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            finish()
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
}