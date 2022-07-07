package com.application.smartstation.ui.model

import okhttp3.MultipartBody

class InputParams {

    //Login
    var phone:String?= null
    var country:String?= null
    var deviceType:String?= null
    var deviceToken:String?= null

    //otp
    var otp:String? = null

    //profile
    var accessToken:String?= null
    var name:String?= null
    var company_name:String?= null
    var company_mail:String?= null
    var profile_pic:MultipartBody.Part?= null

    //edit profile
    var email:String?= null
    var about:String?= null

    //logout
    var user_id:String? = null

    //notification
    var notification_status:String? = null
    var sound:String? = null
    var vibration:String? = null

    //lost phone
    var mail_id:String? = null

    //passcode
    var security_pin:String? = null

    //signature
    var signature:String? = null

    //tfa
    var authenticator_pin:String? = null

    //chat details
    var receiver_id:String? = null

    //file upload
    var file:String? = null

}