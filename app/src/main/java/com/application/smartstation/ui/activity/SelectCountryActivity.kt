package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySelectCountryBinding
import com.application.smartstation.ui.adapter.SelectCountryAdapter
import com.application.smartstation.ui.model.CountryList
import com.application.smartstation.util.CountryUtils
import com.application.smartstation.util.viewBinding

class SelectCountryActivity : BaseActivity() {

    val binding: ActivitySelectCountryBinding by viewBinding()

    var selectCountryAdapter: SelectCountryAdapter? = null
    var list: ArrayList<CountryList> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_country)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.ilHeader.imgHeader.visibility = View.GONE
        binding.ilHeader.imgAudio.visibility = View.GONE
        binding.ilHeader.imgVideo.visibility = View.GONE

        binding.ilHeader.txtHeader.text = resources.getString(R.string.choose_country)

        list.addAll(CountryUtils.getAllCountries(this))

        binding.rvCountry.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL, false)
        selectCountryAdapter = SelectCountryAdapter(this)
        binding.rvCountry.adapter = selectCountryAdapter
        selectCountryAdapter!!.setCountry(list)

        selectCountryAdapter!!.onItemClick = { model ->
            val intent = Intent()
            intent.putExtra("country", model.countryName)
            intent.putExtra("countryFlag", model.countryCode)
            intent.putExtra("countryCode", "+ " + model.countryNum)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val txt = s.toString()
                filterList(txt)

            }
        })

    }

    private fun filterList(txt: String) {
        if (txt != "") {
            val searchtext = txt.toLowerCase()
            val templist: ArrayList<CountryList> = ArrayList()

            for (items in list) {
                val coinsy =
                    items.countryName.toLowerCase() + items.countryCode.toLowerCase() + items.countryNum
                if (coinsy.contains(searchtext)) {
                    templist.add(items)
                }

            }

            selectCountryAdapter?.setCountry(templist)
        } else {
            selectCountryAdapter?.setCountry(list)
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }
    }
}