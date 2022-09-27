package com.application.smartstation.ui.activity.product

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityProductBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.BaseActivity
import com.application.smartstation.ui.adapter.CategoryAdapter
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.ui.model.ProductCateListRes
import com.application.smartstation.util.Constants
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductActivity : BaseActivity() {

    val binding: ActivityProductBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list: ArrayList<ProductCateListRes> = ArrayList()
    var categoryAdapter: CategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.rvCat.layoutManager = GridLayoutManager(this, 2)
        categoryAdapter = CategoryAdapter(this)
        binding.rvCat.adapter = categoryAdapter

        getCategory()

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txt = s.toString()
                filterCatgoryList(txt)

            }
        })
    }

    private fun filterCatgoryList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<ProductCateListRes> = ArrayList()

            for (items in list) {
                val chat = items.category_name.toLowerCase()
                if (chat.contains(searchtext)) {
                    templist.add(items)
                }
            }

            categoryAdapter?.setCloud(templist)
        } else {
            categoryAdapter?.setCloud(list)
        }
    }

    private fun getCategory() {
        val inputParams = InputParams()
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)

        apiViewModel.getCategory(inputParams).observe(this, Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.data
                            if (!list.isNullOrEmpty()) {
                                setData(list)
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

    private fun setData(data: ArrayList<ProductCateListRes>) {
        categoryAdapter!!.setCloud(data)
    }

    private fun setOnClickListener() {
        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgSearch.setOnClickListener {
            binding.llSearch.visibility = View.VISIBLE
            binding.imgSearch.visibility = View.GONE
            binding.imgCancel.visibility = View.VISIBLE
        }

        binding.imgCancel.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(this)
            binding.llSearch.visibility = View.GONE
            binding.imgSearch.visibility = View.VISIBLE
            binding.imgCancel.visibility = View.GONE
        }
    }
}