package com.application.smartstation.service

object ApiUrl {

    const val API ="/api/"
//    const val API ="/"

    const val LOGIN =API+"login"
    const val OTP_VERIFY =API+"checkotp"
    const val RESEND_OTP =API+"resendotp"
    const val UPDATE_PROFILE =API+"profile_update"
    const val IMG_UPLOAD =API+"upload"
    const val TERMS_AND_CONDITION =API+"terms"
    const val ADD_EXPENSE =API+"addexpense"
    const val LOGOUT =API+"logOutStatus"
    const val CHANGE_NOTIFICATION =API+"changenotification_status"
    const val GET_NOTIFICATION =API+"getnotification_status"
    const val SEND_MAIL_OTP =API+"sendemailotp"
    const val UPDATE_PROFILE_PIC =API+"updateProfilePic"
    const val EDIT_PROFILE =API+"editProfile"
    const val GET_PROFILE =API+"getprofile"
    const val SECURITY_PIN =API+"securityupdate"
    const val CHECK_SECURITY_PIN =API+"checksecuritypin"
    const val REMOVE_SECURITY_PIN =API+"removesecuritypin"
    //incomplete
    const val SIGNATURE_UPLOAD =API+"signatureupload"
    const val GET_SIGNATURE =API+"getsignature"
    const val REMOVE_SIGNATURE =API+"removesignature"

    const val CHECK_MAIL_OTP =API+"checkemailotp"
    const val RESEND_MAIL_OTP =API+"resendemailotp"
    const val GET_TFA =API+"google_authenticator"
    const val TFA_UPDATE =API+"google_authenticator_validation"
    const val GET_CHAT_LIST =API+"getrecent_chat"
    const val GET_CHAT_DATA_LIST =API+"getchat_list_details"
    const val GET_MAIL_LIST =API+"getmail_list"
    const val GET_USER_LIST =API+"get_all_user_details"
    const val FILE_UPLOAD =API+"fileupload"
    const val CREATE_GRP =API+"create_group"
    const val GET_GRP_DETAILS =API+"get_group_chat_details"
    const val ADD_GRP_USER =API+"add_group_member"

}