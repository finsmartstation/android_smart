package com.application.smartstation.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewLetterBinding
import com.application.smartstation.service.Status
import com.application.smartstation.tokenautocomplete.CharacterTokenizer
import com.application.smartstation.tokenautocomplete.TokenCompleteTextView
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.Person
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NewLetterActivity : BaseActivity(), TokenCompleteTextView.TokenListener<Person>  {

    val binding:ActivityNewLetterBinding by viewBinding()
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
    var signature = ""
    var stamp = ""

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
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            if (!it.data.data.default_signature.isNullOrEmpty()){
                                binding.clSignature.visibility = View.VISIBLE
                                signature = it.data.data.default_signature
                                Glide.with(this).load(signature).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgSignature)
                            }
                            if (!it.data.data.default_stamp.isNullOrEmpty()){
                                binding.clStamp.visibility = View.VISIBLE
                                stamp = it.data.data.default_stamp
                                Glide.with(this).load(stamp).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.imgStamp)
                            }

                            if (it.data.data.default_signature.isNullOrEmpty() && it.data.data.default_stamp.isNullOrEmpty()){
                                binding.llAttach.visibility = View.GONE
                            }else{
                                binding.llAttach.visibility = View.VISIBLE
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

        binding.imgSignatureClose.setOnClickListener {
            binding.clSignature.visibility = View.GONE
            signature = ""
            if (signature.isNullOrEmpty() && stamp.isNullOrEmpty()){
                binding.llAttach.visibility = View.GONE
            }
        }

        binding.imgStampClose.setOnClickListener {
            binding.clStamp.visibility = View.GONE
            stamp = ""
            if (signature.isNullOrEmpty() && stamp.isNullOrEmpty()){
                binding.llAttach.visibility = View.GONE
            }
        }


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
        TODO("Not yet implemented")
    }
}