package com.application.smartstation.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.application.smartstation.R
import com.application.smartstation.databinding.ActivityChatBinding
import com.application.smartstation.databinding.ActivityChatInfoBinding
import com.application.smartstation.util.Constants
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_chat_info.view.*

@AndroidEntryPoint
class ChatInfoActivity : BaseActivity() {

    val binding: ActivityChatInfoBinding by viewBinding()
    val apiViewModel: ApiViewModel by viewModels()
    var name = ""
    var profilePic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_info)
        initView()
        setOnClickListener()
    }

    private fun initView() {

        if (intent != null){
            name = intent.getStringExtra(Constants.NAME)!!
            profilePic = intent.getStringExtra(Constants.PROFILE_PIC)!!

            binding.txtName.text = name
            Glide.with(this).load(profilePic).placeholder(R.drawable.ic_default).error(R.drawable.ic_default).diskCacheStrategy(
                DiskCacheStrategy.DATA).into(binding.imgProfile)
        }

        binding.appBarLayout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    //when collapsingToolbar at that time display actionbar title
                    binding.toolbar.txtHeader.text = name
                    isShow = true
                } else if (isShow) {
                    binding.toolbar.txtHeader.text = resources.getString(R.string.contact_info)
                    isShow = false
                }
            }
        })
    }

    private fun setOnClickListener() {

    }
}