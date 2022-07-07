package com.application.smartstation.ui.activity

import android.os.Bundle
import android.view.View
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityNewMailBinding
import com.application.smartstation.ui.model.Person
import com.application.smartstation.util.viewBinding
import com.tokenautocomplete.TokenCompleteTextView

class NewMailActivity : BaseActivity(),
    TokenCompleteTextView.TokenListener<Person> {

    private val binding:ActivityNewMailBinding by viewBinding()
    var clickB = true

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

        binding.txtCcVisible.setOnClickListener {
            binding.llCc.visibility = View.VISIBLE
            binding.ivCc.visibility = View.VISIBLE
            binding.txtCcVisible.visibility = View.GONE
        }

        binding.txtBccVisible.setOnClickListener {
            binding.llBcc.visibility = View.VISIBLE
            binding.ivBcc.visibility = View.VISIBLE
            binding.txtBccVisible.visibility = View.GONE
        }

        binding.imgToArrow.setOnClickListener {
            if (clickB){
                clickB = false
                binding.llTo.visibility = View.VISIBLE
                binding.ivTo.visibility = View.VISIBLE
                binding.imgToArrow.setImageResource(R.drawable.ic_up_arrow)
            }else{
                clickB = true
                binding.llTo.visibility = View.GONE
                binding.ivTo.visibility = View.GONE
                binding.imgToArrow.setImageResource(R.drawable.ic_down_arrow)
            }
        }
    }

    private fun initView() {
        binding.edtTo.setTokenListener(this)

        binding.ilHeader.txtHeader.text = resources.getString(R.string.compose)
    }

    override fun onTokenAdded(token: Person?) {

    }

    override fun onTokenRemoved(token: Person?) {

    }

    override fun onTokenIgnored(token: Person?) {

    }
}