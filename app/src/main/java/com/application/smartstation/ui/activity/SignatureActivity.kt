package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySignatureBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.HeaderFooterAdapter
import com.application.smartstation.ui.adapter.SignatureAdapter
import com.application.smartstation.ui.adapter.StampAdapter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.ListHeaderFooterRes
import com.application.smartstation.ui.model.SignatureListData
import com.application.smartstation.ui.model.StampList
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignatureActivity : BaseActivity() {

    val binding: ActivitySignatureBinding by viewBinding()
    var list: ArrayList<SignatureListData> = ArrayList()
    var listStamp: ArrayList<StampList> = ArrayList()
    var listHeaderFooter: ArrayList<ListHeaderFooterRes> = ArrayList()
    var signatureAdapter: SignatureAdapter? = null
    var stampAdapter: StampAdapter? = null
    var headerFooterAdapter: HeaderFooterAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()
    var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (intent != null) {
            type = intent.getIntExtra("type", 0)
            if (type.equals(1)) {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.signature)
            } else if (type.equals(2)) {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.stamp)
            }else if (type.equals(3)) {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.letter_header)
            }else  {
                binding.ilHeader.txtHeader.text = resources.getString(R.string.letter_footer)
            }
            binding.ilHeader.imgAudio.visibility = View.VISIBLE
        }

    }

    private fun deleteSignature(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.image_id = id

        apiViewModel.removeSignature(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getSignature()
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

    private fun getSignature() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getSignature(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.data
                            if (list.isNotEmpty()) {
                                binding.rvSignatureList.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            } else {
                                binding.rvSignatureList.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setData(list: ArrayList<SignatureListData>) {
        binding.rvSignatureList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        signatureAdapter = SignatureAdapter(this, 1)
        binding.rvSignatureList.adapter = signatureAdapter

        signatureAdapter!!.onItemDeleteClick = { model ->
            deleteSignature(model.id)
        }

        signatureAdapter!!.onItemSelectClick = { model ->

        }

        signatureAdapter!!.onItemClick = { model ->
            setSignature(model.id)
        }
        signatureAdapter!!.setSignature(list)
    }

    private fun setSignature(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.setSignature(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getSignature()
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

        binding.ilHeader.imgAudio.setOnClickListener {
            startActivity(Intent(this, UploadSignatureActivity::class.java).putExtra("type", type))
        }
    }

    override fun onResume() {
        super.onResume()
        if (type.equals(1)) {
            getSignature()
        } else if (type.equals(2)){
            getStamp()
        }else if (type.equals(3)){
            getHeader()
        }else {
            getFooter()
        }

    }

    private fun getFooter() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getLetterFooter(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            listHeaderFooter = it.data.data
                            if (listHeaderFooter.isNotEmpty()) {
                                binding.rvSignatureList.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setDataHeaderFooter(listHeaderFooter)
                            } else {
                                binding.txtNoFound.text = resources.getString(R.string.no_footer)
                                binding.rvSignatureList.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun getHeader() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getLetterHeader(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            listHeaderFooter = it.data.data
                            if (listHeaderFooter.isNotEmpty()) {
                                binding.rvSignatureList.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setDataHeaderFooter(listHeaderFooter)
                            } else {
                                binding.txtNoFound.text = resources.getString(R.string.no_header)
                                binding.rvSignatureList.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setDataHeaderFooter(listHeaderFooter: ArrayList<ListHeaderFooterRes>) {
        binding.rvSignatureList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        headerFooterAdapter = HeaderFooterAdapter(this, 1)
        binding.rvSignatureList.adapter = headerFooterAdapter

        headerFooterAdapter!!.onItemDeleteClick = { model ->
            if (type.equals(3)){
                deleteHeader(model.id)
            }else {
                deleteFooter(model.id)
            }

        }

        headerFooterAdapter!!.onItemSelectClick = { model ->

        }

        headerFooterAdapter!!.onItemClick = { model ->
            if (type.equals(3)){
                setHeader(model.id)
            }else {
                setFooter(model.id)
            }
        }
        headerFooterAdapter!!.setHeaderFooter(listHeaderFooter)
    }

    private fun setHeader(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.setLetterHeader(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getHeader()
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

    private fun setFooter(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.setLetterFooter(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getFooter()
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

    private fun deleteHeader(id: String) {

    }

    private fun deleteFooter(id: String) {

    }

    private fun getStamp() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getStamp(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            listStamp = it.data.data
                            if (listStamp.isNotEmpty()) {
                                binding.rvSignatureList.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setDataStamp(listStamp)
                            } else {
                                binding.txtNoFound.text = resources.getString(R.string.no_stamp)
                                binding.rvSignatureList.visibility = View.GONE
                                binding.txtNoFound.visibility = View.VISIBLE
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

    private fun setDataStamp(listStamp: ArrayList<StampList>) {
        binding.rvSignatureList.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        stampAdapter = StampAdapter(this, 1)
        binding.rvSignatureList.adapter = stampAdapter

        stampAdapter!!.onItemDeleteClick = { model ->
            deleteStamp(model.id)
        }

        stampAdapter!!.onItemSelectClick = { model ->

        }

        stampAdapter!!.onItemClick = { model ->
            setStamp(model.id)
        }
        stampAdapter!!.setStamp(listStamp)
    }

    private fun setStamp(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.id = id

        apiViewModel.setStamp(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getStamp()
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

    private fun deleteStamp(id: String) {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.stamp_id = id

        apiViewModel.removeStamp(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            toast(it.data.message)
                            getStamp()
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