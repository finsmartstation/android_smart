package com.application.smartstation.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewMailBinding
import com.application.smartstation.service.Status
import com.application.smartstation.tokenautocomplete.CharacterTokenizer
import com.application.smartstation.tokenautocomplete.TokenCompleteTextView
import com.application.smartstation.ui.adapter.MailFilesAdapter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.MailImageSelect
import com.application.smartstation.ui.model.Person
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Arrays.asList


@AndroidEntryPoint
class NewMailActivity : BaseActivity(),
    TokenCompleteTextView.TokenListener<Person> {

    private val binding: ActivityNewMailBinding by viewBinding()
    var clickB = true
    val apiViewModel: ApiViewModel by viewModels()
    var toMail = ""
    var ccMail = ""
    var bccMail = ""
    var body = ""
    var body1 = 0
    var status = 0
    var toList = ArrayList<String>()
    var ccList = ArrayList<String>()
    var bccList = ArrayList<String>()
    var imageParts: ArrayList<MultipartBody.Part?> = ArrayList()
    var pathList: ArrayList<MailImageSelect> = ArrayList()
    var mailFilesAdapter: MailFilesAdapter? = null

    val mimeTypes = arrayOf(
        "image/jpeg", // jpeg or jpg
        "image/png", // png
        "application/pdf", // pdf
//        "application/msword", // doc
//        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // docx
//        "application/vnd.ms-excel", // xls
//        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // xlsx
//        "video/mp4", // mp4
    )

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
            if (binding.txtCcVisible.visibility.equals(View.GONE) && binding.txtBccVisible.visibility.equals(
                    View.GONE)
            ) {
                binding.ivTo.visibility = View.GONE
            }
        }

        binding.txtBccVisible.setOnClickListener {
            binding.llBcc.visibility = View.VISIBLE
            binding.ivBcc.visibility = View.VISIBLE
            binding.txtBccVisible.visibility = View.GONE
            if (binding.txtCcVisible.visibility.equals(View.GONE) && binding.txtBccVisible.visibility.equals(
                    View.GONE)
            ) {
                binding.ivTo.visibility = View.GONE
            }
        }

        binding.imgToArrow.setOnClickListener {
            if (clickB) {
                clickB = false
                binding.llTo.visibility = View.VISIBLE
                binding.ivTo.visibility = View.VISIBLE
                binding.txtBccVisible.visibility = View.VISIBLE
                binding.txtCcVisible.visibility = View.VISIBLE
                binding.imgToArrow.setImageResource(R.drawable.ic_up_arrow)
            } else {
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
            } else {
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
            } else {
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
            } else {
                bccMail = binding.edtBcc.text.toString().trim()
            }

            val subject = binding.edtSubject.text.toString().trim()

            var bodys = binding.edtBody.text.toString().trim()

            try {
                bodys = bodys.removeRange(body1, bodys.length)
                bodys = bodys + "<br>" + body
            } catch (e: Exception) {
                Log.d("TAG", "setOnClickListener: " + e)
            }


            when {
                TextUtils.isEmpty(toMail) -> toast(resources.getString(R.string.please_enter_to))
                TextUtils.isEmpty(subject) -> toast(resources.getString(R.string.please_enter_subject))
                TextUtils.isEmpty(bodys) -> toast(resources.getString(R.string.please_enter_body))

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
                        inputParams.body = bodys
                        composeMailWithoutImage(inputParams)
                    } else {
                        val user_id: RequestBody =
                            UtilsDefault.getSharedPreferenceString(
                                Constants.USER_ID)!!
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val accessToken: RequestBody =
                            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val to_mail: RequestBody =
                            toMail
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val cc_mail: RequestBody =
                            ccMail
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val bcc_mail: RequestBody =
                            bccMail
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val subject: RequestBody =
                            subject
                                .toRequestBody("text/plain".toMediaTypeOrNull())
                        val body: RequestBody = bodys
                            .toRequestBody("text/plain".toMediaTypeOrNull())

                        composeMail(user_id,
                            accessToken,
                            to_mail,
                            cc_mail,
                            bcc_mail,
                            subject,
                            body,
                            imageParts)
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
        apiViewModel.composeMail(userId,
            accessToken,
            toMail,
            ccMail,
            bccMail,
            subject,
            body,
            imageParts).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            finish()
                            toast(it.data.message)
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
                        } else if (UtilsDefault.isPdfFile(path)) {
                            pathList.add(MailImageSelect(path, "pdf"))
                        }
                        if (!pathList.isNullOrEmpty()) {
                            binding.rvMailList.visibility = View.VISIBLE
                            mailFilesAdapter!!.setMail(pathList)
                        } else {
                            binding.rvMailList.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.d("TAG", "onFilePickerResult: " + e)
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
                    } else if (UtilsDefault.isPdfFile(path)) {
                        pathList.add(MailImageSelect(path, "pdf"))
                    }
                    if (!pathList.isNullOrEmpty()) {
                        binding.rvMailList.visibility = View.VISIBLE
                        mailFilesAdapter!!.setMail(pathList)
                    } else {
                        binding.rvMailList.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.d("TAG", "onFilePickerResult: " + e)
                }
            }
            else -> return
        }
    }

    @SuppressLint("Range")
    private fun prepareFilePart(s: String, uri: Uri?): MultipartBody.Part {
        val path = FileUtils.getPath(this, uri)
        val file = File(path)
        if (UtilsDefault.isImageFile(path)) {
            val compressedImageFile = Compressor(this).setQuality(100).compressToFile(file)
            val requestFile: RequestBody =
                compressedImageFile.asRequestBody("*/*".toMediaTypeOrNull())
            return MultipartBody.Part.createFormData(s, compressedImageFile.name, requestFile)
        }

        val requestFile: RequestBody = file.asRequestBody("*/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(s, file.name, requestFile)
    }

    private fun composeMailWithoutImage(inputParams: InputParams) {
        apiViewModel.composeMailWithoutImage(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            finish()
                            toast(it.data.message)
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

    private fun initView() {
        binding.edtTo.setTokenListener(this)
        binding.edtTo.setTokenizer(CharacterTokenizer(asList(' ', ','), ","))
//        binding.edtTo.performCollapse(false)
        binding.edtCc.setTokenListener(this)
        binding.edtCc.setTokenizer(CharacterTokenizer(asList(' ', ','), ","))
        binding.edtBcc.setTokenListener(this)
        binding.edtBcc.setTokenizer(CharacterTokenizer(asList(' ', ','), ","))

        binding.rvMailList.layoutManager = GridLayoutManager(this, 3)
        mailFilesAdapter = MailFilesAdapter(this)
        binding.rvMailList.adapter = mailFilesAdapter

        mailFilesAdapter!!.onItemClick = { model ->
            pathList.removeAt(model)
            imageParts.removeAt(model)
            if (pathList.isEmpty()) {
                binding.rvMailList.visibility = View.GONE
            }
            mailFilesAdapter!!.notifyDataSetChanged()
        }


        if (intent != null) {
            if (intent.getStringExtra("from") != null) {
                val from = intent.getStringExtra("from")!!
                toList = stringToWords(from)
                for (a in toList) {
                    binding.edtTo.setText(a)
                }
            }

            if (intent.getStringExtra("bcc") != null) {
                val from = intent.getStringExtra("bcc")!!
                bccList = stringToWords(from)
                for (a in bccList) {
                    binding.edtBcc.setText(a)
                }
            }

            if (intent.getStringExtra("to") != null) {
                val to = intent.getStringExtra("to")!!
                ccList.addAll(stringToWords(to))

            }

            if (intent.getStringExtra("cc") != null) {
                val to = intent.getStringExtra("cc")!!
                ccList.addAll(stringToWords(to))
            }

            if (!ccList.isNullOrEmpty()) {
                for (a in ccList) {
                    binding.edtCc.setText(a)
                }
            }

            if (intent.getStringExtra("sub") != null) {
                val sub = intent.getStringExtra("sub")!!
                binding.edtSubject.setText(sub)
            }

            if (intent.getStringExtra("attach") != null) {
                val sub = intent.getStringExtra("attach")!!
                setPdf(sub)
            }

            if (intent.getStringExtra("body") != null) {
                body = intent.getStringExtra("body")!!
                binding.edtBody.setText(Html.fromHtml("<br>" + body))
                binding.edtBody.requestFocus()
                binding.edtBody.setSelection(0)
            }
        }

        binding.svLayout.smoothScrollTo(0, binding.edtTo.top)

        binding.ilHeader.txtHeader.text = resources.getString(R.string.compose)

        binding.edtBody.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.svLayout.smoothScrollTo(0, binding.edtSubject.bottom)
            }
        }

        binding.edtBody.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int,
            ) {

            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int,
            ) {
                body1 = start + count
            }
        })

    }

    private fun setPdf(sub: String) {
        val uri = Uri.fromFile(File(sub))
        try {
            val imageRequest: MultipartBody.Part = prepareFilePart("attachment[]", uri)
            imageParts.add(imageRequest)
            val path = FileUtils.getPath(this, uri)
            if (UtilsDefault.isImageFile(path)) {
                pathList.add(MailImageSelect(path, "img"))
            } else if (UtilsDefault.isPdfFile(path)) {
                pathList.add(MailImageSelect(path, "pdf"))
            }
            if (!pathList.isNullOrEmpty()) {
                binding.rvMailList.visibility = View.VISIBLE
                mailFilesAdapter!!.setMail(pathList)
            } else {
                binding.rvMailList.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.d("TAG", "onFilePickerResult: " + e)
        }
    }

    private fun stringToWords(mnemonic: String): ArrayList<String> {
        val words = ArrayList<String>()
        for (w in mnemonic.trim(' ').split(",")) {
            if (w.isNotEmpty()) {
                words.add(w)
            }
        }
        return words
    }


    override fun onTokenAdded(token: Person) {
        if (status.equals(0)) {
            toList.add(token.email)
        } else if (status.equals(1)) {
            ccList.add(token.email)
        } else if (status.equals(2)) {
            bccList.add(token.email)
        }
    }

    override fun onTokenRemoved(token: Person) {
        try {
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
            } else if (status.equals(2)) {
                for (i in 0 until bccList.size) {
                    if (token.email.equals(bccList[i])) {
                        bccList.remove(bccList[i])
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "onTokenRemoved: " + e)
        }

    }

    override fun onTokenIgnored(token: Person) {
        TODO("Not yet implemented")
    }
}