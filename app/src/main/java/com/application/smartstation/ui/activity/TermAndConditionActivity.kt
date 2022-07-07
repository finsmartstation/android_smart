package com.application.smartstation.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityTermAndConditionBinding
import com.application.smartstation.service.Status
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermAndConditionActivity : BaseActivity() {

    val binding: ActivityTermAndConditionBinding by viewBinding()

    val apiViewModel: ApiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_and_condition)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if(intent != null){
            val type = intent.getStringExtra("type")
            if (type.equals("terms")){
                getTerms()
            }
        }
    }

    private fun getTerms() {
        apiViewModel.getTerms().observe(this, Observer {
            it.let {
                when(it.status){
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
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

    }
}