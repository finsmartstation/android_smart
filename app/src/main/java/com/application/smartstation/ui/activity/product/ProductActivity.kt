package com.application.smartstation.ui.activity.product

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityProductBinding
import com.application.smartstation.ui.activity.BaseActivity
import com.application.smartstation.ui.adapter.CategoryAdapter
import com.application.smartstation.ui.adapter.ProductsAdapter
import com.application.smartstation.ui.model.ProductCateListRes
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductActivity : BaseActivity() {

    val binding: ActivityProductBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var list:ArrayList<ProductCateListRes> = ArrayList()
    var listproduct:ArrayList<ProductCateListRes> = ArrayList()
    var categoryAdapter:CategoryAdapter? = null
    var productsAdapter: ProductsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        binding.rvCat.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        categoryAdapter = CategoryAdapter(this)
        binding.rvCat.adapter = categoryAdapter

        binding.rvProducts.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL,false)
        productsAdapter = ProductsAdapter(this)
        binding.rvProducts.adapter = productsAdapter

        list.add(ProductCateListRes(1, "Trending","","",0))
        list.add(ProductCateListRes(2, "Most Popular","","",0))
        list.add(ProductCateListRes(3, "All Body Products","","",0))
        list.add(ProductCateListRes(4, "Skin Care","","",0))
        list.add(ProductCateListRes(5, "Hair Care","","",0))
        list.add(ProductCateListRes(6, "Make Up","","",0))
        list.add(ProductCateListRes(7, "Fragrance","","",0))

        categoryAdapter!!.setCloud(list)

        listproduct.add(ProductCateListRes(1,
            "Japanese Cherry Blossom",
            "250 ml",
            "$ 17.00",
            R.drawable.prod2))
        listproduct.add(ProductCateListRes(2,
            "African Mango Shower Gel",
            "350 ml",
            "$ 25.00",
            R.drawable.prod1))
        listproduct.add(ProductCateListRes(1,
            "Japanese Cherry Blossom",
            "250 ml",
            "$ 17.00",
            R.drawable.prod2))
        listproduct.add(ProductCateListRes(2,
            "African Mango Shower Gel",
            "350 ml",
            "$ 25.00",
            R.drawable.prod1))
        listproduct.add(ProductCateListRes(1,
            "Japanese Cherry Blossom",
            "250 ml",
            "$ 17.00",
            R.drawable.prod2))
        listproduct.add(ProductCateListRes(2,
            "African Mango Shower Gel",
            "350 ml",
            "$ 25.00",
            R.drawable.prod1))

        productsAdapter!!.setCloud(listproduct)

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