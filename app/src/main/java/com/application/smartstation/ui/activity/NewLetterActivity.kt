package com.application.smartstation.ui.activity

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewLetterBinding
import com.application.smartstation.databinding.BottomSheetDialogSignatureBinding
import com.application.smartstation.service.Status
import com.application.smartstation.tokenautocomplete.CharacterTokenizer
import com.application.smartstation.tokenautocomplete.TokenCompleteTextView
import com.application.smartstation.ui.adapter.SignatureAdapter
import com.application.smartstation.ui.adapter.StampAdapter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.Person
import com.application.smartstation.ui.model.SignatureListData
import com.application.smartstation.ui.model.StampList
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NewLetterActivity : BaseActivity(), TokenCompleteTextView.TokenListener<Person> {

    val binding: ActivityNewLetterBinding by viewBinding()
    var clickB = true
    val apiViewModel: ApiViewModel by viewModels()
    var toMail = ""
    var ccMail = ""
    var bccMail = ""
    var body = ""
    var status = 0
    var toList = ArrayList<String>()
    var ccList = ArrayList<String>()
    var bccList = ArrayList<String>()
    var signatureList = ArrayList<SignatureListData>()
    var stampList = ArrayList<StampList>()
    var signature = ""
    var stamp = ""
    var signatureAdapter: SignatureAdapter? = null
    var stampAdapter: StampAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_letter)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.imgAttach.visibility = View.GONE
        binding.ilHeader.txtHeader.text = resources.getString(R.string.new_letter)
        binding.edtTo.setTokenListener(this)
        binding.edtTo.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))
        binding.edtCc.setTokenListener(this)
        binding.edtCc.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))
        binding.edtBcc.setTokenListener(this)
        binding.edtBcc.setTokenizer(CharacterTokenizer(Arrays.asList(' ', ','), ","))

        getStampSignature()

    }

    private fun getStampSignature() {
        val inputParams = InputParams()
        inputParams.user_id =
            UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken =
            UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        apiViewModel.getStampSignature(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {

                            if (!it.data.data.signature.isNullOrEmpty()) {
                                signatureList = it.data.data.signature
                            }

                            if (!it.data.data.stamp.isNullOrEmpty()) {
                                stampList = it.data.data.stamp
                            }

                            if (!it.data.data.default_signature.isNullOrEmpty()) {
                                binding.clSignature.visibility = View.VISIBLE
                                signature = it.data.data.default_signature
                                Glide.with(this).load(signature).placeholder(R.drawable.ic_default)
                                    .error(R.drawable.ic_default)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .into(binding.imgSignature)
                            }
                            if (!it.data.data.default_stamp.isNullOrEmpty()) {
                                binding.clStamp.visibility = View.VISIBLE
                                stamp = it.data.data.default_stamp
                                Glide.with(this).load(stamp).placeholder(R.drawable.ic_default)
                                    .error(R.drawable.ic_default)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .into(binding.imgStamp)
                            }

                            if (it.data.data.default_signature.isNullOrEmpty() && it.data.data.default_stamp.isNullOrEmpty()) {
                                binding.llAttach.visibility = View.GONE
                            } else {
                                binding.llAttach.visibility = View.VISIBLE
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

        binding.imgSignatureClose.setOnClickListener {
            binding.clSignature.visibility = View.GONE
            signature = ""
            if (signature.isNullOrEmpty() && stamp.isNullOrEmpty()) {
                binding.llAttach.visibility = View.GONE
            }
        }

        binding.imgStampClose.setOnClickListener {
            binding.clStamp.visibility = View.GONE
            stamp = ""
            if (signature.isNullOrEmpty() && stamp.isNullOrEmpty()) {
                binding.llAttach.visibility = View.GONE
            }
        }

        binding.llSignature.setOnClickListener {
            if (signatureList.isNullOrEmpty()) {
                toast("Please upload your signature")
            } else {
                showSignatureBottomDialog(1)
            }
        }

        binding.llStamp.setOnClickListener {
            if (stampList.isNullOrEmpty()) {
                toast("Please upload your stamp")
            } else {
                showSignatureBottomDialog(2)
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
            val bodys = binding.edtBody.text.toString().trim()
            when {
                TextUtils.isEmpty(toMail) -> toast(resources.getString(R.string.please_enter_to))
                TextUtils.isEmpty(subject) -> toast(resources.getString(R.string.please_enter_subject))
                TextUtils.isEmpty(bodys) -> toast(resources.getString(R.string.please_enter_body))

                else -> {
                    val inputParams = InputParams()
                    inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
                    inputParams.accessToken =
                        UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
                    inputParams.to_mail = toMail
                    inputParams.cc_mail = ccMail
                    inputParams.bcc_mail = bccMail
                    inputParams.subject = subject
                    inputParams.mail_body = subject
                    inputParams.letter_body = bodys
                    inputParams.signature_url_path = signature
                    inputParams.stamp_url_path = stamp
                    createLetter(inputParams)
                }
            }
        }
    }

    private fun showSignatureBottomDialog(i: Int) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog_signature, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.setOnShowListener { setupBottomSheet(it) }
        val bind = BottomSheetDialogSignatureBinding.bind(view)

//        val bottomSheetBehavior:BottomSheetBehavior<View> = BottomSheetBehavior.from(view.parent as View)
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//        bind.clView.minimumHeight = Resources.getSystem().displayMetrics.heightPixels

        bind.rvSignStamp.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)

        if (i.equals(1)) {
            bind.txtHeader.text = resources.getString(R.string.signature)
            signatureAdapter = SignatureAdapter(this, 2)
            bind.rvSignStamp.adapter = signatureAdapter
            signatureAdapter!!.setSignature(signatureList)

            signatureAdapter!!.onItemDeleteClick = { model ->

            }

            signatureAdapter!!.onItemClick = { model ->

            }

            signatureAdapter!!.onItemSelectClick = { model ->
                dialog.dismiss()
                Log.d("TAG", "showSignatureBottomDialog:" + model.image)
                binding.llAttach.visibility = View.VISIBLE
                binding.clSignature.visibility = View.VISIBLE
                signature = model.image
                Glide.with(this).load(model.image).placeholder(R.drawable.ic_default)
                    .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.imgSignature)
            }

        } else {
            bind.txtHeader.text = resources.getString(R.string.stamp)
            stampAdapter = StampAdapter(this, 2)
            bind.rvSignStamp.adapter = stampAdapter
            stampAdapter!!.setStamp(stampList)

            stampAdapter!!.onItemDeleteClick = { model ->

            }

            stampAdapter!!.onItemClick = { model ->

            }


            stampAdapter!!.onItemSelectClick = { model ->
                dialog.dismiss()
                binding.llAttach.visibility = View.VISIBLE
                binding.clStamp.visibility = View.VISIBLE
                stamp = model.image
                Glide.with(this).load(stamp).placeholder(R.drawable.ic_default)
                    .error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(binding.imgStamp)
            }
        }
        dialog.show()
    }

    private fun setupBottomSheet(it: DialogInterface?) {
        val bottomSheetDialog = it as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun createLetter(inputParams: InputParams) {
        apiViewModel.newLetter(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            finish()
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
    }

    override fun onTokenIgnored(token: Person) {
        TODO("Not yet implemented")
    }
}