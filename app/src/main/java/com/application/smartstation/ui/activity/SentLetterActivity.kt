package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySentLetterBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.adapter.SentboxLetterAdapter
import com.application.smartstation.ui.model.DataSentLetter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SentLetterActivity : BaseActivity() {

    val binding: ActivitySentLetterBinding by viewBinding()
    var list: ArrayList<DataSentLetter> = ArrayList()
    var sentboxLetterAdapter: SentboxLetterAdapter? = null
    val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sent_letter)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.txtHeader.text = resources.getString(R.string.sentbox)
        binding.rvSentBox.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        sentboxLetterAdapter = SentboxLetterAdapter(this)
        binding.rvSentBox.adapter = sentboxLetterAdapter

        sentboxLetterAdapter!!.onItemClick = { model ->
            val intent = Intent(this, ViewLetterActivity::class.java)
            intent.putExtra("boxType", 2)
            intent.putExtra("id", model.id)
            startActivity(intent)
        }


    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        getSentBox()
    }

    private fun getSentBox() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getLetterSent(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.data.letter_sent_list
                            if (list.isNotEmpty()) {
                                binding.rvSentBox.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            } else {
                                binding.rvSentBox.visibility = View.GONE
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

    private fun setData(list: ArrayList<DataSentLetter>) {
        sentboxLetterAdapter!!.setMail(list)
    }
}