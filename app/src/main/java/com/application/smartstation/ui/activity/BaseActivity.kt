package com.application.smartstation.ui.activity

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.application.smartstation.R
import com.application.smartstation.util.*
import com.application.smartstation.view.LoadingDialog
import kotlin.random.Random
import kotlin.random.nextInt


open class BaseActivity: AppCompatActivity(), UtilInterface {

    var mcontext: Context? = null
    private val permissions: HashMap<Int, (permissionGranted: Boolean) -> Unit> = HashMap()
    private val progressDialog: LoadingDialog by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        mcontext = this
    }

    fun setStatusBar(){
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            window.setBackgroundDrawableResource(R.drawable.button_gradient)
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           if (UtilsDefault.getSharedPreferenceValuefcm(Constants.THEME)== "dark"){
               window!!.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
               window!!.statusBarColor = ContextCompat.getColor(this, R.color.purple_200)
           }
           else {
               window!!.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
               window!!.statusBarColor = ContextCompat.getColor(this,R.color.white)
           }




       }

    }

    fun setPrimaryStatusBar(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_chat_popup_gray)
//            window.setBackgroundDrawableResource(R.drawable.ic_splash_bg_use)
        }
    }

    override fun toast(message: String) {
        ToastUtils.show(this, message)
    }

    fun imagePermission(action: () -> Unit){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA){
                if(it){
                    action()
                }else{
                    toast("Need Camera and Storage Permission!")
                }
            }
        }else{
            action()
        }
    }

    fun phnPermission(action: () -> Unit){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(
                Manifest.permission.READ_PHONE_STATE){
                if(it){
                    action()
                }else{
                    toast("Need Phone Permission!")
                }
            }
        }else{
            action()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.permissionResult(requestCode, permissions, grantResults, this.permissions)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun requestPermission(
        vararg permissions: String,
        result: (permissionGranted: Boolean) -> Unit
    ) {
        var requestCode = 0
        do {
            requestCode = Random.nextInt(0..10000)
        } while (this.permissions.containsKey(requestCode))
        this.permissions[requestCode] = result
        PermissionUtils.requestPermission(this, requestCode, *permissions)
    }

    override fun showProgress() {
        progressDialog.show()
    }

    override fun isProgressShowing(): Boolean = progressDialog.isShowing()

    override fun dismissProgress() {
        progressDialog.dismiss()

    }
}