package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewMailBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.MailFilesAdapter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.MailImageSelect
import com.application.smartstation.ui.model.Person
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.tokenautocomplete.TokenCompleteTextView
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


@AndroidEntryPoint
class NewMailActivity : BaseActivity(),
    TokenCompleteTextView.TokenListener<Person> {

    private val binding:ActivityNewMailBinding by viewBinding()
    var clickB = true
    val apiViewModel: ApiViewModel by viewModels()
    var toMail = ""
    var ccMail = ""
    var bccMail = ""
    var status = 0
    var toList = ArrayList<String>()
    var ccList = ArrayList<String>()
    var bccList = ArrayList<String>()
    var imageParts: ArrayList<MultipartBody.Part?> = ArrayList()
    var pathList: ArrayList<MailImageSelect> = ArrayList()
    var mailFilesAdapter:MailFilesAdapter? = null

    val mimeTypes = arrayOf(
        "image/jpeg", // jpeg or jpg
        "image/png", // png
        "application/pdf", // pdf
        "application/msword", // doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
        "application/vnd.ms-excel", // xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
        "video/mp4", // mp4
    )

    private val IMAGE_DIRECTORY = "/demonuts_upload_gallery"
    private val BUFFER_SIZE = 1024 * 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_mail)
        initView()
        setOnClickListener()
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

        binding.ilHeader.imgAttach.setOnClickListener {
            imagePermission {
                startFilePicker()
            }
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

            for (s in 0 until toList.size) {
                if (s.equals(0)){
                    toMail = toList[s]
                }else{
                    toMail = toMail+", "+toList[s]
                }
            }

            for (s in 0 until ccList.size) {
                if (s.equals(0)){
                    ccMail = ccList[s]
                }else{
                    ccMail = ccMail+", "+ccList[s]
                }
            }

            for (s in 0 until bccList.size) {
                if (s.equals(0)){
                    bccMail = bccList[s]
                }else{
                    bccMail = bccMail+", "+bccList[s]
                }
            }

            val subject = binding.edtSubject.text.toString().trim()
            val body = binding.edtBody.text.toString().trim()

            when {
                TextUtils.isEmpty(toMail) -> toast(resources.getString(R.string.please_enter_to))
                TextUtils.isEmpty(subject) -> toast(resources.getString(R.string.please_enter_subject))
                TextUtils.isEmpty(body) -> toast(resources.getString(R.string.please_enter_body))

                else -> {
                    if (imageParts.isEmpty()) {
                        val inputParams = InputParams()
                        inputParams.user_id =
                            UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                        inputParams.accessToken =
                            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                        inputParams.to_mail = toMail
                        inputParams.cc_mail = ccMail
                        inputParams.bcc_mail = bccMail
                        inputParams.subject = subject
                        inputParams.body = body
                        composeMailWithoutImage(inputParams)
                    }else{
                        val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(
                            Constants.USER_ID)!!)
                        val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                        )
                        val to_mail: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            toMail
                        )
                        val cc_mail: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            ccMail
                        )
                        val bcc_mail: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            bccMail
                        )
                        val subject: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            subject
                        )
                        val body: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            body
                        )
                        
                        composeMail(user_id,accessToken,to_mail,cc_mail,bcc_mail,subject,body,imageParts)
                    }
                }
            }

        }
    }

    private fun composeMail(
        userId: RequestBody,
        accessToken: RequestBody,
        toMail: RequestBody,
        ccMail: RequestBody,
        bccMail: RequestBody,
        subject: RequestBody,
        body: RequestBody,
        imageParts: List<MultipartBody.Part?>,
    ) {
        apiViewModel.composeMail(userId,accessToken,toMail,ccMail,bccMail,subject,body,imageParts).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            finish()
                            toast(it.data.message)
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

    private fun startFilePicker() {
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
        pickerIntent.type = "*/*"
        pickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        filePickerLauncher.launch(pickerIntent)
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::onFilePickerResult
    )

    private fun onFilePickerResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        val multipleUriData = result.data?.clipData
        val singleUri = result.data?.data

        when {
            multipleUriData != null -> {
                for (i in 0 until multipleUriData.itemCount) {
                    val uri = multipleUriData.getItemAt(i).uri
                    try {
                        val imageRequest: MultipartBody.Part = prepareFilePart("attachment[]", uri)
                        imageParts.add(imageRequest)
                        val path = FileUtils.getPath(this, uri)
                        if (UtilsDefault.isImageFile(path)) {
                            pathList.add(MailImageSelect(path, "img"))
                        }else if (UtilsDefault.isPdfFile(path)){
                            pathList.add(MailImageSelect(path,"pdf"))
                        }
                        if (!pathList.isNullOrEmpty()){
                            binding.rvMailList.visibility = View.VISIBLE
                            mailFilesAdapter!!.setMail(pathList)
                        }else{
                            binding.rvMailList.visibility = View.GONE
                        }
                    }catch (e:Exception){
                        Log.d("TAG", "onFilePickerResult: "+e)
                    }

                }
            }
            singleUri != null -> {
                val uri = singleUri
                try {
                    val imageRequest: MultipartBody.Part = prepareFilePart("attachment[]", uri)
                    imageParts.add(imageRequest)
                    val path = FileUtils.getPath(this, uri)
                    if (UtilsDefault.isImageFile(path)) {
                        pathList.add(MailImageSelect(path, "img"))
                    }else if (UtilsDefault.isPdfFile(path)){
                        pathList.add(MailImageSelect(path,"pdf"))
                    }
                    if (!pathList.isNullOrEmpty()){
                        binding.rvMailList.visibility = View.VISIBLE
                        mailFilesAdapter!!.setMail(pathList)
                    }else{
                        binding.rvMailList.visibility = View.GONE
                    }
                }catch (e:Exception){
                    Log.d("TAG", "onFilePickerResult: "+e)
                }
            }
            else -> return
        }
    }

    @SuppressLint("Range")
    private fun prepareFilePart(s: String, uri: Uri?): MultipartBody.Part {
        val path = FileUtils.getPath(this,uri)
        var file = File(path)
        if (UtilsDefault.isImageFile(path)){
            val compressedImageFile = Compressor(this).setQuality(100).compressToFile(file)
            val requestFile: RequestBody = RequestBody.create(
                "*/*".toMediaTypeOrNull(),
                compressedImageFile)
            return MultipartBody.Part.createFormData(s, compressedImageFile.name, requestFile)
        }

        val requestFile: RequestBody = RequestBody.create(
            "*/*".toMediaTypeOrNull(),
            file)
        return MultipartBody.Part.createFormData(s, file.name, requestFile)
    }

    private fun composeMailWithoutImage(inputParams: InputParams) {
        apiViewModel.composeMailWithoutImage(inputParams).observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            finish()
                            toast(it.data.message)
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

    private fun initView() {
        binding.edtTo.setTokenListener(this)
        binding.edtCc.setTokenListener(this)
        binding.edtBcc.setTokenListener(this)

        if (intent != null){
            if (intent.getStringExtra("to") != null){
                toMail = intent.getStringExtra("to")!!
            }

            if (intent.getStringExtra("sub") != null){
               val sub = intent.getStringExtra("sub")!!
                binding.edtSubject.setText(sub)
            }

            if (intent.getStringExtra("body") != null) {
                val body = intent.getStringExtra("body")!!
                binding.edtBody.setText(Html.fromHtml("<br>" + body))
                binding.edtBody.requestFocus()
                binding.edtBody.setSelection(0)
            }
        }

        binding.svLayout.smoothScrollTo(0, binding.edtTo.top)

        binding.ilHeader.txtHeader.text = resources.getString(R.string.compose)

        binding.rvMailList.layoutManager = GridLayoutManager(this,2)
        mailFilesAdapter = MailFilesAdapter(this)
        binding.rvMailList.adapter = mailFilesAdapter

        mailFilesAdapter!!.onItemClick = { model ->
            pathList.removeAt(model)
            imageParts.removeAt(model)
            if (pathList.isEmpty()){
                binding.rvMailList.visibility = View.GONE
            }
            mailFilesAdapter!!.notifyDataSetChanged()
        }

        binding.edtBody.setOnFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.svLayout.smoothScrollTo(0, binding.edtSubject.bottom)
            }
        })
    }

    override fun onTokenAdded(token: Person?) {
        if (status.equals(0)) {
            toList.add(token!!.email)
        }else if (status.equals(1)) {
            ccList.add(token!!.email)
        }else if (status.equals(2)) {
            bccList.add(token!!.email)
        }
    }

    override fun onTokenRemoved(token: Person?) {
        if (status.equals(0)) {
            for (i in 0 until toList.size) {
                if (token!!.email.equals(toList[i])) {
                    toList.remove(toList[i])
                }
            }
        } else if (status.equals(1)) {
            for (i in 0 until ccList.size) {
                if (token!!.email.equals(ccList[i])) {
                    ccList.remove(ccList[i])
                }
            }
        }else if (status.equals(2)) {
            for (i in 0 until bccList.size) {
                if (token!!.email.equals(bccList[i])) {
                    bccList.remove(bccList[i])
                }
            }
        }
    }

    override fun onTokenIgnored(token: Person?) {

    }
}