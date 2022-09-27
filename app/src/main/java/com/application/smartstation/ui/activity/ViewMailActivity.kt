package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityViewMailBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.adapter.MailAttachAdapter
import com.application.smartstation.ui.model.DataInbox
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils.startsWith

@AndroidEntryPoint
class ViewMailActivity : BaseActivity() {

    val binding: ActivityViewMailBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var type = 0
    var sub = ""
    var from = ""
    var date = ""
    var body = ""
    var profile_pic = ""
    var id = ""
    var to:Array<String>? = null
    var to1:Array<String>? = null
    var cc:Array<String>? = null
    var bcc:Array<String>? = null
    var attachmentList:ArrayList<String>? = ArrayList()
    var mailFilesAdapter: MailAttachAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_mail)
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
            getInboxDetails(id)
        }else{
            binding.ilHeader.txtHeader.text = resources.getString(R.string.sentbox)
            getSentDetails(id)
        }


        binding.rvMailList.layoutManager = GridLayoutManager(this,3)
        mailFilesAdapter = MailAttachAdapter(this)
        binding.rvMailList.adapter = mailFilesAdapter
        mailFilesAdapter!!.onItemClick = { model ->

            UtilsDefault.downloadFile(this, attachmentList!![model],"Mail",object :MailCallback{
                override fun success(resp: String?, status: Boolean?) {
                    if (status!!){
                        if (resp!!.contains(".pdf")){
                            startActivity(Intent(this@ViewMailActivity,PdfViewActivity::class.java).putExtra("path",resp))
                        }else {
                            FileUtils.openDocument(this@ViewMailActivity, resp)
                        }
                    }
                }

            })
        }
    }

    private fun getSentDetails(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.getSent(inputParams).observe(this, Observer {
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

    private fun getInboxDetails(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.getInbox(inputParams).observe(this, Observer {
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

    private fun setData(data: DataInbox) {
        from = data.from
        to = data.to
        sub = data.subject
        cc = data.cc
        bcc = data.bcc
        if (type.equals(1)){
            date = data.created_datetime
        }else{
            date = data.sent_datetime
        }

        body = data.body

        attachmentList!!.clear()
        if (!data.attachments.isNullOrEmpty()){
            for (i in 0 until data.attachments.size){
                attachmentList!!.add(data.attachments[i])
            }
        }

        if (attachmentList!!.isNotEmpty()){
            binding.rvMailList.visibility = View.VISIBLE
            mailFilesAdapter!!.setMail(attachmentList!!)
        }else{
            binding.rvMailList.visibility = View.GONE
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

        binding.ilHeader.imgDelete.setOnClickListener {
            deleteMail()
        }

        binding.btnReply.setOnClickListener {
            val intent = Intent(this,NewMailActivity::class.java)
            intent.putExtra("from",from)
            intent.putExtra("sub",subjectVal(sub))
            intent.putExtra("body",bodyMsg(body,date))
            startActivity(intent)
        }

        binding.btnReplyAll.setOnClickListener {
            val intent = Intent(this,NewMailActivity::class.java)
            intent.putExtra("from",from)
            intent.putExtra("cc",cc)
            intent.putExtra("to",to)
            intent.putExtra("bcc",bcc)
            intent.putExtra("sub",subjectVal(sub))
            intent.putExtra("body",bodyMsg(body,date))
            startActivity(intent)
        }
        
        binding.btnForward.setOnClickListener {
            val intent = Intent(this,NewMailActivity::class.java)
            intent.putExtra("sub",subjectFwdVal(sub))
            intent.putExtra("body",bodyMsgFwd(body,date))
            startActivity(intent)
        }

        binding.btnForward.setOnClickListener {
            val intent = Intent(this,NewMailActivity::class.java)
            intent.putExtra("sub",subjectFwdVal(sub))
            intent.putExtra("body",bodyMsgFwd(body,date))
            startActivity(intent)
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
    }

    private fun bodyMsgFwd(body: String, date: String): String? {
        val s = "<br>"+
                "<div class=\"gmail_quote\">"+
                "<div dir=\"ltr\" class=\"gmail_attr\">---------- Forwarded message ---------<br>From: <span dir=\"auto\">&lt;<a href=\"mailto:$from\">$from</a>&gt;</span><br>Date: "+
                "On ${UtilsDefault.replyDayMail(date)}, ${UtilsDefault.replyTime(date)} at ${UtilsDefault.todayDate(UtilsDefault.localTimeConvert(date))}<br>Subject: $sub<br>To: &lt;<a href=\"$to\">$to</a>&gt;<br></div><br><br>"+
                "$body<br></div></div>\r\n"
        return s
    }

    private fun subjectFwdVal(sub: String): String? {
        if (sub.startsWith("Fwd:")){
            return sub
        }else if (sub.startsWith("Re:")){
            return sub.replace("Re:","Fwd:")
        }
        return "Fwd: "+sub
    }

    private fun deleteMail() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id
        if (type.equals(1)) {
            inputParams.type = "inbox"
        }else{
            inputParams.type = "send"
        }

        apiViewModel.deleteMail(inputParams).observe(this, Observer {
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

    fun subjectVal(values: String):String {
        if (values.startsWith("Re:")){
            return sub
        }else if (sub.startsWith("Fwd:")){
            return sub.replace("Fwd:","Re:")
        }
        return "Re: "+sub
    }


    fun bodyMsg(values: String, date: String):String {
        val s = "<br>"+
        "<div class=\"gmail_quote\">"+
        "<div dir=\"ltr\" class=\"gmail_attr\">On ${UtilsDefault.replyDayMail(date)}, ${UtilsDefault.replyTime(date)} at ${UtilsDefault.todayDate(UtilsDefault.localTimeConvert(date))} &lt;<a href=\"mailto:$from\">$from</a>&gt; wrote:<br></div>"+
        "<blockquote class=\"gmail_quote\" style=\"margin:0px 0px 0px 0.8ex;border-left:1px solid"+
        "rgb(204,204,204);padding-left:1ex\">$values</blockquote> </div>\r\n"
        return s
    }
}