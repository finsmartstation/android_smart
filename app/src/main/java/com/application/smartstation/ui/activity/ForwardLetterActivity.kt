package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityForwardLetterBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.tokenautocomplete.CharacterTokenizer
import com.application.smartstation.tokenautocomplete.TokenCompleteTextView
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.Person
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ForwardLetterActivity : BaseActivity(), TokenCompleteTextView.TokenListener<Person> {

    val binding: ActivityForwardLetterBinding by viewBinding()
    var clickB = true
    val apiViewModel: ApiViewModel by viewModels()
    var toMail = ""
    var ccMail = ""
    var bccMail = ""
    var path = ""
    var body = ""
    var id = ""
    var status = 0
    var toList = ArrayList<String>()
    var ccList = ArrayList<String>()
    var bccList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forward_letter)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.imgAttach.visibility = View.GONE
        binding.ilHeader.txtHeader.text = resources.getString(R.string.forward_letter)
        binding.edtTo.setTokenListener(this)
        binding.edtTo.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))
        binding.edtCc.setTokenListener(this)
        binding.edtCc.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))
        binding.edtBcc.setTokenListener(this)
        binding.edtBcc.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))

        if (intent != null) {
            if (intent.getStringExtra("sub") != null) {
                val sub = intent.getStringExtra("sub")!!
                binding.edtSubject.setText(sub)
                binding.edtBody.setText(sub)
            }


            if (intent.getStringExtra("id") != null) {
                id = intent.getStringExtra("id")!!
            }

            if (intent.getStringExtra("attach") != null) {
                path = intent.getStringExtra("attach")!!
                if(!path.isNullOrEmpty()){
                    binding.clPdf.visibility = View.VISIBLE
                    val fileName = FileUtils.getFileNameFromPath(path).replace("/","")
                    binding.txtFileName.text = fileName
                }
            }
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.edtTo.setOnClickListener {
            status = 0
        }

        binding.edtCc.setOnClickListener {
            status = 1
        }

        binding.edtBcc.setOnClickListener {
            status = 2
        }

        binding.clPdf.setOnClickListener {
            UtilsDefault.downloadFile(this, path,"Letter",object : MailCallback {
                override fun success(resp: String?, status: Boolean?) {
                    if (status!!){
                        if (resp!!.contains(".pdf")){
                            startActivity(Intent(this@ForwardLetterActivity,PdfViewActivity::class.java).putExtra("path",resp))
                        }else {
                            FileUtils.openDocument(this@ForwardLetterActivity, resp)
                        }
                    }
                }

            })
        }

        binding.txtCcVisible.setOnClickListener {
            binding.llCc.visibility = View.VISIBLE
            binding.ivCc.visibility = View.VISIBLE
            binding.txtCcVisible.visibility = View.GONE
            if (binding.txtCcVisible.visibility.equals(View.GONE) && binding.txtBccVisible.visibility.equals(View.GONE)){
                binding.ivTo.visibility = View.GONE
            }
        }

        binding.txtBccVisible.setOnClickListener {
            binding.llBcc.visibility = View.VISIBLE
            binding.ivBcc.visibility = View.VISIBLE
            binding.txtBccVisible.visibility = View.GONE
            if (binding.txtCcVisible.visibility.equals(View.GONE) && binding.txtBccVisible.visibility.equals(View.GONE)){
                binding.ivTo.visibility = View.GONE
            }
        }

        binding.imgToArrow.setOnClickListener {
            if (clickB){
                clickB = false
                binding.llTo.visibility = View.VISIBLE
                binding.ivTo.visibility = View.VISIBLE
                binding.txtBccVisible.visibility = View.VISIBLE
                binding.txtCcVisible.visibility = View.VISIBLE
                binding.imgToArrow.setImageResource(R.drawable.ic_up_arrow)
            }else{
                clickB = true
                binding.llTo.visibility = View.GONE
                binding.ivTo.visibility = View.GONE
                binding.llCc.visibility = View.GONE
                binding.ivCc.visibility = View.GONE
                binding.llBcc.visibility = View.GONE
                binding.ivBcc.visibility = View.GONE
                binding.edtCc.setText("")
                binding.edtBcc.setText("")
                binding.imgToArrow.setImageResource(R.drawable.ic_down_arrow)
            }
        }

        binding.ilHeader.imgSend.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)

            if (!toList.isNullOrEmpty()) {
                for (s in 0 until toList.size) {
                    if (s.equals(0)) {
                        toMail = toList[s]
                    } else {
                        toMail = toMail + ", " + toList[s]
                    }
                }
            }else{
                toMail = binding.edtTo.text.toString().trim()
            }

            if (!ccList.isNullOrEmpty()) {
                for (s in 0 until ccList.size) {
                    if (s.equals(0)) {
                        ccMail = ccList[s]
                    } else {
                        ccMail = ccMail + ", " + ccList[s]
                    }
                }
            }else{
                ccMail = binding.edtCc.text.toString().trim()
            }

            if (!bccList.isNullOrEmpty()) {
                for (s in 0 until bccList.size) {
                    if (s.equals(0)) {
                        bccMail = bccList[s]
                    } else {
                        bccMail = bccMail + ", " + bccList[s]
                    }
                }
            }else{
                bccMail = binding.edtBcc.text.toString().trim()
            }

            val subject = binding.edtSubject.text.toString().trim()
            val bodys = binding.edtBody.text.toString().trim()
            when {
                TextUtils.isEmpty(toMail) -> toast(resources.getString(R.string.please_enter_to))
                TextUtils.isEmpty(subject) -> toast(resources.getString(R.string.please_enter_subject))
                TextUtils.isEmpty(bodys) -> toast(resources.getString(R.string.please_enter_body))

                else -> {
                    val inputParams = InputParams()
                    inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                    inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                    inputParams.to_mail = toMail
                    inputParams.cc = ccMail
                    inputParams.bcc = bccMail
                    inputParams.subject = subject
                    inputParams.body = body
                    inputParams.id = id
                    forwardLetter(inputParams)
                }
            }
        }

    }

    private fun forwardLetter(inputParams: InputParams) {
        apiViewModel.forwardLetter(inputParams).observe(this, androidx.lifecycle.Observer {
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

    override fun onTokenAdded(token: Person) {
        if (status.equals(0)) {
            toList.add(token.email)
        }else if (status.equals(1)) {
            ccList.add(token.email)
        }else if (status.equals(2)) {
            bccList.add(token.email)
        }
    }

    override fun onTokenRemoved(token: Person) {
        if (status.equals(0)) {
            for (i in 0 until toList.size) {
                if (token.email.equals(toList[i])) {
                    toList.remove(toList[i])
                }
            }
        } else if (status.equals(1)) {
            for (i in 0 until ccList.size) {
                if (token.email.equals(ccList[i])) {
                    ccList.remove(ccList[i])
                }
            }
        }else if (status.equals(2)) {
            for (i in 0 until bccList.size) {
                if (token.email.equals(bccList[i])) {
                    bccList.remove(bccList[i])
                }
            }
        }
    }

    override fun onTokenIgnored(token: Person) {
    }
}