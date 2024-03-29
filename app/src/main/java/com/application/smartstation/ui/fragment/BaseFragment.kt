package com.application.smartstation.ui.fragment

import android.Manifest
import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.application.smartstation.R
import com.application.smartstation.ui.activity.BaseActivity
import com.application.smartstation.util.Constants
import com.application.smartstation.util.ToastUtils
import com.application.smartstation.util.UtilInterface
import com.application.smartstation.util.UtilsDefault

open class BaseFragment(layoutId: Int) : Fragment(layoutId), UtilInterface {


    override fun toast(message: String) {
        ToastUtils.show(requireContext(), message)
    }

    override fun requestPermission(
        vararg permissions: String,
        result: (permissionGranted: Boolean) -> Unit,
    ) {
        (activity as UtilInterface).requestPermission(*permissions, result = result)
    }

    override fun showProgress() {
        (activity as BaseActivity?)?.showProgress()
    }

    fun isLoggedIn(): Boolean {
        return UtilsDefault.getSharedPreferenceString(Constants.LOGIN_STATUS) != null
    }

    override fun isProgressShowing(): Boolean =
        (activity as BaseActivity?)?.isProgressShowing() ?: false

    override fun dismissProgress() {
        (activity as BaseActivity?)?.dismissProgress()
    }


    fun imagePermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA) {
                if (it) {
                    action()
                } else {
                    toast("Need Camera and Storage Permission!")
                }
            }
        } else {
            action()
        }
    }

    fun phnPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(
                Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS) {
                if (it) {
                    action()
                } else {
                    toast("Need Phone Permission!")
                }
            }
        } else {
            action()
        }
    }


    fun setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (UtilsDefault.getSharedPreferenceValuefcm(Constants.THEME) == "dark") {
                activity?.window!!.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                activity?.window!!.statusBarColor =
                    ContextCompat.getColor(requireActivity(), R.color.purple_200)
            } else {
                activity?.window!!.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                activity?.window!!.statusBarColor =
                    ContextCompat.getColor(requireActivity(), R.color.purple_200)
            }


        }

    }

    fun cleardata(v: EditText) {
        v.setText("")

    }


}