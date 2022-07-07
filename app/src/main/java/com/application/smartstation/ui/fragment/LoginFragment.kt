package com.application.smartstation.ui.fragment
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.application.smartstation.R
import com.application.smartstation.databinding.ConfrimMobileBottomDialogBinding
import com.application.smartstation.databinding.FragmentLoginBinding
import com.application.smartstation.service.Status
import com.application.smartstation.ui.activity.LoginActivity
import com.application.smartstation.ui.activity.LoginActivity.Companion.loginBack
import com.application.smartstation.ui.activity.SelectCountryActivity
import com.application.smartstation.ui.model.InputParams
import com.application.smartstation.util.Constants
import com.application.smartstation.util.CountryUtils
import com.application.smartstation.util.UtilsDefault
import com.application.smartstation.util.viewBinding
import com.application.smartstation.viewmodel.ApiViewModel
import com.google.android.gms.auth.api.credentials.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)

    val apiViewModel: ApiViewModel by viewModels()

    private val RESULT_CODE = 124
    var countryNum = ""
    var mobileNum = ""
    private val RC_HINT = 1000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClickListener()

    }

    private fun initView() {

        val telephoneManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCode = telephoneManager.networkCountryIso

        for (i in 0 until CountryUtils.getAllCountries(requireActivity()).size){
            if (CountryUtils.getAllCountries(requireActivity())[i].countryCode.equals(countryCode)){
                countryNum = "+ "+CountryUtils.getAllCountries(requireActivity())[i].countryNum
                Log.d("TAG", "initView: "+countryNum)
            }
        }

//        phnPermission {
//
//        }

        binding.txtCountry.text = UtilsDefault.countryImg(countryCode.toUpperCase())+" "+countryCode.toUpperCase()+" "+countryNum

//        val SpanString = SpannableString(
//            binding.txtLoginSub.text.toString()
//        )
//
//        val permissionNum: ClickableSpan = object : ClickableSpan() {
//            override fun onClick(textView: View) {
//                phnPermission {
//                    showHint()
//                }
//            }
//
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//                ds.isUnderlineText = false
//            }
//        }
//
//
//        SpanString.setSpan(permissionNum, 52, 69, 0)
//        SpanString.setSpan(RelativeSizeSpan(1f), 52, 69, 0)
//        SpanString.setSpan(ForegroundColorSpan(resources.getColor(R.color.color_dark_green)), 52, 69, 0)
//
//        binding.txtLoginSub.setMovementMethod(LinkMovementMethod.getInstance())
//        binding.txtLoginSub.setText(SpanString, TextView.BufferType.SPANNABLE)
//        binding.txtLoginSub.setSelected(true)

    }



    private fun showHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()

        val credentialsClient = Credentials.getClient(requireActivity(), options)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(
                intent.intentSender,
                RC_HINT, null, 0, 0, 0, Bundle()
            )
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }

    }


    private fun setOnClickListener() {

        binding.llCountry.setOnClickListener {
            startActivityForResult(Intent(requireActivity(),SelectCountryActivity::class.java),RESULT_CODE)
        }

        binding.txtLost.setOnClickListener {
            (activity as LoginActivity).fragmentHelper?.push(LostMailFragment())
        }

        binding.btnLogin.setOnClickListener {
            UtilsDefault.hideKeyboardForFocusedView(requireActivity())
            mobileNum = binding.edtMobile.text.toString().trim()

            when {

                TextUtils.isEmpty(countryNum) -> toast(requireActivity().resources.getString(R.string.please_select_country))
                TextUtils.isEmpty(mobileNum) -> toast(requireActivity().resources.getString(R.string.please_enter_mobile))
                mobileNum.length < 6 || mobileNum.length > 17  -> toast(requireActivity().resources.getString(R.string.please_enter_valid_mobile))

                else -> {
                    dialogConfrim(mobileNum,countryNum)
                }

            }
        }
    }

    private fun loginApi(inputParams: InputParams) {
        apiViewModel.login(inputParams).observe(requireActivity(), Observer {
            it?.let {
                when(it.status){

                    Status.LOADING -> {
                        showProgress()
                    }
                    Status.SUCCESS -> {
                        dismissProgress()
                        if (it.data!!.status){
                            val otpFragment = OTPFragment()
                            val bundle = Bundle()
                            bundle.putString(Constants.MOB, mobileNum)
                            bundle.putString(Constants.COUNTRY_NUM, countryNum)
                            bundle.putInt(Constants.OTP, it.data.otp)
                            otpFragment.arguments = bundle
                            (activity as LoginActivity).fragmentHelper?.push(otpFragment)
                        }else{
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

    private fun dialogConfrim(mobileNum: String, countryNum: String) {
        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.confrim_mobile_bottom_dialog, null)
        val bind = ConfrimMobileBottomDialogBinding.bind(view)
        dialog.setCancelable(false)

        dialog.setOnShowListener {
            (view!!.parent as View).setBackgroundColor(Color.TRANSPARENT)
        }

        dialog.setContentView(view)
        dialog.show()

        bind.txtNumber.text = countryNum+" "+mobileNum

        bind.txtEdit.setOnClickListener {
            dialog.dismiss()
        }

        bind.txtOk.setOnClickListener {
            dialog.dismiss()
            val inputParams = InputParams()
            inputParams.country = countryNum.replace("+ ","")
            inputParams.phone = mobileNum.replace("(","").replace(")","").replace("-","").replace(" ","")
            inputParams.deviceType = "android"
            inputParams.deviceToken = UtilsDefault.getSharedPreferenceValuefcm(Constants.FCM_KEY)
            loginApi(inputParams)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == RESULT_CODE) {
            val name = data.getStringExtra("country")
            val flag = data.getStringExtra("countryFlag")
            countryNum = data.getStringExtra("countryCode")!!
            binding.txtCountry.post {
                binding.txtCountry.setText(UtilsDefault.countryImg(flag!!.toUpperCase())+" "+flag!!.toUpperCase()+" "+countryNum)
            }

        }else if (requestCode == RC_HINT && resultCode == AppCompatActivity.RESULT_OK) {

            // get data from the dialog which is of type Credential
            val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)

            // set the received data t the text view
            credential?.apply {
                binding.edtMobile.setText(UtilsDefault.PhoneNumberWithoutCountryCode(credential.id))
                Log.d("TAG", "onActivityResult: "+credential.id+"  "+UtilsDefault.PhoneNumberWithoutCountryCode(credential.id))
            }
        } else if (requestCode == RC_HINT && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            Toast.makeText(requireActivity(), "No phone numbers found", Toast.LENGTH_LONG).show()
        }

    }



    override fun onResume() {
        super.onResume()
        loginBack = 0
    }
}