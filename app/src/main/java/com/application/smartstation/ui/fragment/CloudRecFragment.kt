package com.application.smartstation.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.application.smartstation.R
import com.application.smartstation.databinding.FragmentCloudRecBinding
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.CloudFolderActivity
import com.application.smartstation.ui.activity.PdfViewActivity
import com.application.smartstation.ui.adapter.CloudViewAdapter
import com.application.smartstation.ui.model.CloudDetailListRes
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.FileUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CloudRecFragment(var phn: String) : BaseFragment(R.layout.fragment_cloud_rec) {

    private val binding by viewBinding(FragmentCloudRecBinding::bind)
    val apiViewModel: ApiViewModel by viewModels()
    var list: ArrayList<CloudDetailListRes> = ArrayList()
    var cloudViewAdapter: CloudViewAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()
    }

    private fun initView() {
        binding.rvCloudView.layoutManager = GridLayoutManager(requireActivity(), 3)
        cloudViewAdapter = CloudViewAdapter(requireActivity())
        binding.rvCloudView.adapter = cloudViewAdapter

        cloudViewAdapter!!.onItemClick = { model ->
            if (model.file_type.equals("folder")) {
                startActivity(Intent(requireActivity(), CloudFolderActivity::class.java)
                    .putExtra("id", model.id)
                    .putExtra("name", model.name)
                    .putExtra("type", "send"))
            } else {
                UtilsDefault.downloadFile(requireActivity(),
                    model.file_path, "Cloud",
                    object : MailCallback {
                        override fun success(resp: String?, status: Boolean?) {
                            if (status!!) {
                                if (resp!!.contains(".pdf")) {
                                    startActivity(Intent(requireActivity(),
                                        PdfViewActivity::class.java).putExtra("path", resp))
                                } else {
                                    FileUtils.openDocument(requireActivity(), resp)
                                }
                            }
                        }

                    })
            }
        }
    }

    private fun setOnClickListener() {

    }

    override fun onResume() {
        super.onResume()
        getCloudRec()
    }

    private fun getCloudRec() {
        val inputParams = InputParams()
        inputParams.accessToken = UtilsDefault.getSharedPreferenceString(Constants.ACCESS_TOKEN)
        inputParams.user_id = UtilsDefault.getSharedPreferenceString(Constants.USER_ID)
        inputParams.parent_folder_id = phn

        apiViewModel.getCloudDetails(inputParams).observe(requireActivity(), Observer {
            it.let {
                when (it.status) {
                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status) {
                            list = it.data.received_datas
                            if (!list.isNullOrEmpty()) {
                                binding.rvCloudView.visibility = View.VISIBLE
                                binding.txtNoFound.visibility = View.GONE
                                setData(list)
                            } else {
                                binding.rvCloudView.visibility = View.GONE
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

    private fun setData(list: ArrayList<CloudDetailListRes>) {
        cloudViewAdapter!!.setCloud(list)
    }

}