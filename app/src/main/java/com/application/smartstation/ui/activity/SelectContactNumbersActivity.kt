package com.application.smartstation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivitySelectContactNumbersBinding
import com.application.smartstation.ui.adapter.NumbersForContactAdapter
import com.application.smartstation.ui.model.ExpandableContact
import com.application.smartstation.util.Constants
import com.application.smartstation.util.ContactUtils
import com.application.smartstation.util.viewBinding
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class SelectContactNumbersActivity : BaseActivity() {

    val binding: ActivitySelectContactNumbersBinding by viewBinding()

    var adapter:NumbersForContactAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_contact_numbers)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        if (!intent.hasExtra(Constants.EXTRA_CONTACT_LIST)) return

        val result: List<ExpandableContact> =
            intent.getParcelableArrayListExtra(Constants.EXTRA_CONTACT_LIST)!!

        adapter = NumbersForContactAdapter(result)


        //EXPAND ALL GROUPS
        adapter!!.toggleAllGroups()

        setItemsChecked(adapter!!)

        binding.rvContact.setLayoutManager(LinearLayoutManager(this))
        binding.rvContact.setAdapter(adapter)
    }

    private fun setItemsChecked(adapter: NumbersForContactAdapter) {
        for (i in 0 until adapter.getGroups().size) {
            val group: MultiCheckExpandableGroup = adapter.getGroups().get(i) as MultiCheckExpandableGroup
            for (x in group.items.indices) {
                group.checkChild(x)
            }
        }
    }

    private fun setOnClickListener() {
        binding.ilHeader.imgBack.setOnClickListener {
            finish()
        }

        binding.fbSendContact.setOnClickListener{ //getting selected numbers from contacts
            val contactNameList: List<ExpandableContact?> =
                ContactUtils.getContactsFromExpandableGroups(adapter!!.getGroups() as List<ExpandableGroup<*>?>)
            val intent = Intent()
            intent.putParcelableArrayListExtra(Constants.EXTRA_CONTACT_LIST,
                contactNameList as ArrayList<out Parcelable?>)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}