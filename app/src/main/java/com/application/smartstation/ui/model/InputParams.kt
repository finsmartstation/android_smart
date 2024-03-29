package com.application.smartstation.ui.model

import okhttp3.MultipartBody

class InputParams {

    //Login
    var phone: String? = null
    var country: String? = null
    var deviceType: String? = null
    var deviceToken: String? = null

    //otp
    var otp: String? = null

    //profile
    var accessToken: String? = null
    var name: String? = null
    var company_name: String? = null
    var company_mail: String? = null
    var profile_pic: MultipartBody.Part? = null

    //edit profile
    var email: String? = null
    var about: String? = null

    //logout
    var user_id: String? = null

    //notification
    var notification_status: String? = null
    var sound: String? = null
    var vibration: String? = null

    //lost phone
    var mail_id: String? = null

    //passcode
    var security_pin: String? = null

    //signature
    var signature: String? = null

    //tfa
    var authenticator_pin: String? = null

    //chat details
    var receiver_id: String? = null

    //file upload
    var file: String? = null

    //grp details
    var group_id: String? = null

    //add user grp
    var members: String? = null

    //compose mail
    var to_mail: String? = null
    var address_to: String? = null
    var cc_mail: String? = null
    var bcc_mail: String? = null
    var subject: String? = null
    var body: String? = null
    var header_url_path: String? = null
    var footer_url_path: String? = null

    //inbox details
    var id: String? = null
    var type: String? = null

    //signature details
    var image_id: String? = null

    //stamp details
    var stamp_id: String? = null

    //header
    var header_id:String? = null

    //footer
    var footer_id:String? = null

    //letter
    var mail_body: String? = null
    var cc: String? = null
    var bcc: String? = null
    var letter_body: String? = null
    var signature_url_path: String? = null
    var stamp_url_path: String? = null

    //cloud
    var parent_folder_id: String? = null
    var folder_name: String? = null
    var access_period: String? = null
    var period_limit: String? = null
    var sub_parent_folder_id: String? = null

    //change grp details
    var group_name:String? = null
    var new_admin_user_id:String? = null
    var remove_user_id:String? = null
    var description:String? = null

    //public show
    var status:String? = null

    //mute notification
    var show_notification:String? = null

}