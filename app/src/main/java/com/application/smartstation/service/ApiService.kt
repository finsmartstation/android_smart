package com.application.smartstation.service

import com.application.smartstation.ui.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface ApiService {

    @POST(ApiUrl.LOGIN)
    suspend fun login(@Body inputParams: InputParams): LoginResponse

    @POST(ApiUrl.OTP_VERIFY)
    suspend fun otpVerify(@Body inputParams: InputParams): OTPVerifyResponse

    @POST(ApiUrl.RESEND_OTP)
    suspend fun resendOTP(@Body inputParams: InputParams): LoginResponse

    @Multipart
    @POST(ApiUrl.UPDATE_PROFILE)
    suspend fun updateProfile(@Part("user_id")user_id:RequestBody,@Part("accessToken")accessToken:RequestBody,@Part("name")name:RequestBody,@Part profile_pic: MultipartBody.Part): ProfileUpdateResponse

    @Multipart
    @POST(ApiUrl.IMG_UPLOAD)
    suspend fun imgUpload(@Part image: MultipartBody.Part): ImageUploadResponse

    @GET(ApiUrl.TERMS_AND_CONDITION)
    suspend fun getTerms(): TermsResponse

    @GET(ApiUrl.ADD_EXPENSE)
    suspend fun addExpense(@Body inputParams: InputParams): LoginResponse

    @POST(ApiUrl.LOGOUT)
    suspend fun logout(@Body inputParams: InputParams): LoginResponse

    @POST(ApiUrl.CHANGE_NOTIFICATION)
    suspend fun changeNotification(@Body inputParams: InputParams): ChangeNotification

    @POST(ApiUrl.GET_NOTIFICATION)
    suspend fun getNotification(@Body inputParams: InputParams): ChangeNotification

    @POST(ApiUrl.SEND_MAIL_OTP)
    suspend fun sendMailOTP(@Body inputParams: InputParams): BaseResponse

    @Multipart
    @POST(ApiUrl.UPDATE_PROFILE_PIC)
    suspend fun updateProfilePic(@Part("user_id")user_id:RequestBody,@Part("accessToken")accessToken:RequestBody,@Part profile_pic: MultipartBody.Part): UpdateProfilePicResponse

    @POST(ApiUrl.EDIT_PROFILE)
    suspend fun editProfile(@Body inputParams: InputParams): EditProfileResponse

    @POST(ApiUrl.GET_PROFILE)
    suspend fun getProfile(@Body inputParams: InputParams): ProfileUpdateResponse

    @POST(ApiUrl.SECURITY_PIN)
    suspend fun passcodeUpdate(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.CHECK_SECURITY_PIN)
    suspend fun passcodeCheck(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.REMOVE_SECURITY_PIN)
    suspend fun passcodeRemove(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_SIGNATURE)
    suspend fun getSignature(@Body inputParams: InputParams): GetSignatureResponse

    @POST(ApiUrl.REMOVE_SIGNATURE)
    suspend fun removeSignature(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.CHECK_MAIL_OTP)
    suspend fun checkEmailOTP(@Body inputParams: InputParams): OTPVerifyResponse

    @POST(ApiUrl.RESEND_MAIL_OTP)
    suspend fun resendEmailOTP(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_TFA)
    suspend fun getTFA(@Body inputParams: InputParams): GetTFAResponse

    @POST(ApiUrl.TFA_UPDATE)
    suspend fun updateTFA(@Body inputParams: InputParams): GetTFAResponse

    @POST(ApiUrl.GET_CHAT_LIST)
    suspend fun getChatlist(@Body inputParams: InputParams): GetChatListResponse

    @POST(ApiUrl.GET_CHAT_DATA_LIST)
    suspend fun getChatDetailslist(@Body inputParams: InputParams): GetChatDetailsListResponse

    @POST(ApiUrl.GET_MAIL_LIST)
    suspend fun getMaillist(@Body inputParams: InputParams): GetMailListResponse

    @POST(ApiUrl.GET_USER_LIST)
    suspend fun getUserlist(@Body inputParams: InputParams): GetUserListResponse

    @POST(ApiUrl.GET_GRP_DETAILS)
    suspend fun getGrpDetails(@Body inputParams: InputParams): GetChatDetailsListResponse

    @POST(ApiUrl.GET_GRP_USER_LIST)
    suspend fun getGrpUserList(@Body inputParams: InputParams): GrpUserListRes

    @POST(ApiUrl.ADD_GRP_USER)
    suspend fun addGrpUser(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.COMPOSE_MAIL)
    suspend fun composeMailWithoutImage(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.SEND_MAIL)
    suspend fun sendMail(@Body inputParams: InputParams): SendMailRes

    @POST(ApiUrl.GET_INBOX)
    suspend fun getInbox(@Body inputParams: InputParams): InboxDetailsRes

    @POST(ApiUrl.GET_SENT)
    suspend fun getSent(@Body inputParams: InputParams): InboxDetailsRes

    @POST(ApiUrl.DELETE_MAIL)
    suspend fun deleteMail(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_STAMP)
    suspend fun getStamp(@Body inputParams: InputParams): GetStampRes

    @POST(ApiUrl.REMOVE_STAMP)
    suspend fun removeStamp(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.SET_SIGNATURE)
    suspend fun setSignature(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.SET_STAMP)
    suspend fun setStamp(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.NEW_LETTER_SENT)
    suspend fun newLetter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_SIGNATURE_STAMP)
    suspend fun getStampSignature(@Body inputParams: InputParams): GetStampSignature

    @Multipart
    @POST(ApiUrl.SIGNATURE_UPLOAD)
    suspend fun uploadSignature(@Part("user_id") user_id:RequestBody, @Part("accessToken") accessToken:RequestBody,@Part("name") group_name:RequestBody,@Part signature: MultipartBody.Part): BaseResponse

    @Multipart
    @POST(ApiUrl.STAMP_UPLOAD)
    suspend fun stampUpload(@Part("user_id") user_id:RequestBody, @Part("accessToken") accessToken:RequestBody,@Part("name") group_name:RequestBody,@Part stamp: MultipartBody.Part): BaseResponse

    @Multipart
    @POST(ApiUrl.CREATE_GRP)
    suspend fun grpCreate(@Part("user_id") user_id:RequestBody, @Part("accessToken") accessToken:RequestBody, @Part("group_name") group_name:RequestBody, @Part("members") members: RequestBody, @Part group_profile: MultipartBody.Part): BaseResponse

    @Multipart
    @POST(ApiUrl.FILE_UPLOAD)
    suspend fun fileUpload(@Part("user_id")user_id:RequestBody,@Part("accessToken")accessToken:RequestBody,@Part file: MultipartBody.Part): BaseResponse

    @Multipart
    @POST(ApiUrl.COMPOSE_MAIL)
    suspend fun composeMail(@Part("user_id") user_id:RequestBody, @Part("accessToken") accessToken:RequestBody, @Part("to_mail") to_mail:RequestBody, @Part("cc_mail") cc_mail:RequestBody, @Part("bcc_mail") bcc_mail:RequestBody, @Part("subject") subject:RequestBody, @Part("body") body:RequestBody, @Part attachment: List<MultipartBody.Part?>): BaseResponse

}