package com.application.smartstation.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.DialogFileBinding
import com.application.smartstation.databinding.DialogFolderBinding
import com.application.smartstation.databinding.FragmentCloudRecBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.CloudFolderActivity
import com.application.smartstation.ui.activity.PdfViewActivity
import com.application.smartstation.ui.adapter.CloudViewAdapter
import com.application.smartstation.ui.model.CloudDetailListRes
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


@AndroidEntryPoint
class CloudSendFragment(var phn: String) : BaseFragment(R.layout.fragment_cloud_rec), AdapterView.OnItemSelectedListener {

    private val binding by viewBinding(FragmentCloudRecBinding::bind)
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<CloudDetailListRes> = ArrayList()
    var cloudViewAdapter: CloudViewAdapter? = null
    var showBtn:Animation? = null
    var hideBtn:Animation? = null
    var showLay:Animation? = null
    var hideLay:Animation? = null
    var time = ""
    var hrs = "life_time"
    var timeFolder = ""
    var hrsFolder = "life_time"
    var hrsArray = arrayOf("hourly", "days", "months","year")
    val timeArray = IntArray(100) { (it + 1) }
    var path:MultipartBody.Part? = null
    var uris:Uri? = null
    var imgUpload:ImageView? = null
    var txtFileName:TextView? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.rvCloudView.layoutManager = GridLayoutManager(requireActivity(), 3)
        cloudViewAdapter = CloudViewAdapter(requireActivity())
        binding.rvCloudView.adapter = cloudViewAdapter

        binding.llFab.visibility = View.VISIBLE

        showBtn  = AnimationUtils.loadAnimation(requireActivity(),R.anim.show_btn)
        hideBtn  = AnimationUtils.loadAnimation(requireActivity(),R.anim.hide_btn)
        showLay  = AnimationUtils.loadAnimation(requireActivity(),R.anim.show_layout)
        hideLay  = AnimationUtils.loadAnimation(requireActivity(),R.anim.hide_layout)


        cloudViewAdapter!!.onItemClick = { model ->
            if (model.file_type.equals("folder")){
                startActivity(Intent(requireActivity(),CloudFolderActivity::class.java)
                    .putExtra("id",model.id)
                    .putExtra("name",model.name)
                    .putExtra("type","send"))
            }else {
                UtilsDefault.downloadFile(requireActivity(),
                    model.file_path,"Cloud",
                    object : MailCallback {
                        override fun success(resp: String?, status: Boolean?) {
                            if (status!!) {
                                if (resp!!.contains(".pdf")) {
                                    startActivity(Intent(requireActivity(),
                                        PdfViewActivity::class.java).putExtra("path", resp))
                                } else {
                                    FileUtils.openDocument(requireActivity(), resp)
                                }
                            }
                        }

                    })
            }
        }
    }

    private fun setOnClickListener() {
        binding.fabAdd.setOnClickListener {
            if(binding.llFolder.visibility == View.VISIBLE && binding.llFile.visibility == View.VISIBLE){
                binding.llFolder.visibility = View.GONE
                binding.llFile.visibility = View.GONE
                binding.llFolder.startAnimation(hideLay)
                binding.llFile.startAnimation(hideLay)
                binding.fabAdd.startAnimation(hideBtn)
            }else{
                binding.llFolder.visibility = View.VISIBLE
                binding.llFile.visibility = View.VISIBLE
                binding.llFolder.startAnimation(showLay)
                binding.llFile.startAnimation(showLay)
                binding.fabAdd.startAnimation(showBtn)
            }
        }

        binding.fabFile.setOnClickListener {
            binding.llFolder.visibility = View.GONE
            binding.llFile.visibility = View.GONE
            binding.llFolder.startAnimation(hideLay)
            binding.llFile.startAnimation(hideLay)
            binding.fabAdd.startAnimation(hideBtn)
            fileDialog()
        }

        binding.fabFolder.setOnClickListener {
            binding.llFolder.visibility = View.GONE
            binding.llFile.visibility = View.GONE
            binding.llFolder.startAnimation(hideLay)
            binding.llFile.startAnimation(hideLay)
            binding.fabAdd.startAnimation(hideBtn)
            folderDialog()
        }
    }

    private fun folderDialog() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_folder, null)
            val dialogFile = BottomSheetDialog(requireActivity())
            dialogFile.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            val bind = DialogFolderBinding.bind(view)
            dialogFile.setCancelable(false)

            bind.spHrsFolder.setOnItemSelectedListener(this)
            bind.spTimeFolder.setOnItemSelectedListener(this)

            val hrs= ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, hrsArray)
            hrs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bind.spHrsFolder.adapter = hrs

            val timeLsit:ArrayList<String> = ArrayList()
            timeLsit.clear()
            for (i in 0 until timeArray.size){
                timeLsit.add(timeArray[i].toString())
            }
            val time= ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, timeLsit)
            time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bind.spTimeFolder.adapter = time

            bind.btnFile.setOnClickListener {
                UtilsDefault.hideKeyboardForFocusedView(requireActivity())
                val name = bind.edtFolderName.text.toString().trim()
                when {
                    TextUtils.isEmpty(name) -> toast(requireActivity().resources.getString(R.string.please_enter_folder))

                    else -> {
                        val inputParams = InputParams()
                        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                        inputParams.parent_folder_id = phn
                        inputParams.folder_name = name
                        inputParams.access_period = hrsFolder
                        inputParams.period_limit = timeFolder
                        createFolder(inputParams)
                        dialogFile.dismiss()
                    }
                }
            }

            bind.rgTime.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbtnFree -> {
                        bind.llTime.visibility = View.GONE
                        hrsFolder = "life_time"
                    }
                    R.id.rbtnTime -> {
                        bind.llTime.visibility = View.VISIBLE
                    }
                }
            })

            bind.imgClose.setOnClickListener {
                dialogFile.dismiss()
            }

            dialogFile.setContentView(view)
            dialogFile.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createFolder(inputParams: InputParams) {
        apiViewModel.createCloudSubFolder(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            getCloudRec()
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

    private fun fileDialog() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_file, null)
            val dialogFile = BottomSheetDialog(requireActivity())

            imgUpload = view.findViewById(R.id.imgUploads)
            txtFileName = view.findViewById(R.id.txtFileName)
            dialogFile!!.setOnShowListener {
                (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
            }

            val bind = DialogFileBinding.bind(view)
            dialogFile.setCancelable(false)

            bind.spHrs.setOnItemSelectedListener(this)
            bind.spTime.setOnItemSelectedListener(this)

            val hrs= ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, hrsArray)
            hrs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bind.spHrs.adapter = hrs

            val timeLsit:ArrayList<String> = ArrayList()
            timeLsit.clear()
            for (i in 0 until timeArray.size){
                timeLsit.add(timeArray[i].toString())
            }
            val time= ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, timeLsit)
            time.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bind.spTime.adapter = time

            bind.imgUploads.setOnClickListener {
                imagePermission {
                    startFilePicker()
                }
            }

            bind.btnFile.setOnClickListener {
                UtilsDefault.hideKeyboardForFocusedView(requireActivity())
                val paths = FileUtils.getPath(requireActivity(), uris)
                var type = ""
                if (UtilsDefault.isImageFile(paths)){
                    type = "image"
                }
                if (UtilsDefault.isPdfFile(paths)){
                    type = "pdf"
                }

                when {
                    TextUtils.isEmpty(paths) -> toast(requireActivity().resources.getString(R.string.please_upload))

                    else -> {
                        val user_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UtilsDefault.getSharedPreferenceString(Constants.USER_ID)!!)
                        val accessToken: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)!!
                        )
                        val parent_folder_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            phn
                        )
                        val access_period: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            hrs.toString()
                        )
                        val period_limit: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            time.toString()
                        )

                        val file_type: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
                            type
                        )

                        val files = File(paths)
                        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), files)
                        val file = MultipartBody.Part.createFormData("file", files.getName(), requestBody)

                        createFile(user_id,accessToken,parent_folder_id,access_period,period_limit,file_type,file)
                        dialogFile.dismiss()
                    }
                }
            }

            bind.rgTime.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbtnFree -> {
                        bind.llTime.visibility = View.GONE
                    }
                    R.id.rbtnTime -> {
                        bind.llTime.visibility = View.VISIBLE
                    }
                }
            })

            bind.imgClose.setOnClickListener {
                dialogFile.dismiss()
            }

            dialogFile.setContentView(view)
            dialogFile.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createFile(
        user_id: RequestBody,
        accessToken: RequestBody,
        parent_folder_id: RequestBody,
        access_period: RequestBody,
        period_limit: RequestBody,
        file_type: RequestBody,
        path: MultipartBody.Part?
    ) {
        apiViewModel.fileUploadCloud(user_id, accessToken, parent_folder_id,access_period,period_limit,file_type,path!!).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            toast(it.data.message)
                            getCloudRec()
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
        pickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
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

            }
            singleUri != null -> {
                val uri = singleUri
                try {
                    uris = singleUri
                    Glide.with(requireActivity()).load(uri).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                        DiskCacheStrategy.DATA).into(imgUpload!!)
                    val paths = FileUtils.getPath(requireActivity(), uri)
                    val fileName = FileUtils.getFileNameFromPath(paths).replace("/","")
                    txtFileName!!.text = fileName
                }catch (e:Exception){
                    Log.d("TAG", "onFilePickerResult: "+e)
                }
            }
            else -> return
        }
    }

    @SuppressLint("Range")
    private fun prepareFilePart(s: String, uri: Uri?): MultipartBody.Part {
        val path = FileUtils.getPath(requireActivity(),uri)
        val file = File(path)
        if (UtilsDefault.isImageFile(path)){
            val compressedImageFile = Compressor(requireActivity()).setQuality(100).compressToFile(file)
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

    override fun onResume() {
        super.onResume()
        getCloudRec()
    }

    private fun getCloudRec() {
        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.parent_folder_id = phn

        apiViewModel.getCloudDetails(inputParams).observe(requireActivity(), Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            list = it.data.send_datas
                            if (!list.isNullOrEmpty()){
                                binding.rvCloudView.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            }else{
                                binding.rvCloudView.visibility = View.GONE
                                binding.txtNoFound.text = requireActivity().resources.getString(R.string.no_send_cloud)
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setData(list: ArrayList<CloudDetailListRes>) {
        cloudViewAdapter!!.setCloud(list)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        (p0!!.getChildAt(0) as TextView).setTextColor(Color.BLACK)
        if (p0.id == R.id.spHrs){
            hrs = hrsArray[p2]
        }

        if (p0.id == R.id.spHrsFolder){
            hrsFolder = hrsArray[p2]
        }

        if (p0.id == R.id.spTime){
            time = timeArray[p2].toString()
        }

        if (p0.id == R.id.spTimeFolder){
            timeFolder = timeArray[p2].toString()
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}


