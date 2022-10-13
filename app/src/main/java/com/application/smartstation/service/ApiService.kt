package com.application.smartstation.service

import com.application.smartstation.ui.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    suspend fun updateProfile(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("name") name: RequestBody,
        @Part profile_pic: MultipartBody.Part,
    ): ProfileUpdateResponse

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
    suspend fun updateProfilePic(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part profile_pic: MultipartBody.Part,
    ): UpdateProfilePicResponse

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

    @POST(ApiUrl.DELETE_LETTER)
    suspend fun deleteLetter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_STAMP)
    suspend fun getStamp(@Body inputParams: InputParams): GetStampRes

    @POST(ApiUrl.REMOVE_STAMP)
    suspend fun removeStamp(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.REMOVE_HEADER)
    suspend fun removeHeader(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.REMOVE_FOOTER)
    suspend fun removeFooter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.BLOCK_USER)
    suspend fun userBlock(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.UNBLOCK_USER)
    suspend fun userUnblock(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.REMOVE_USER_GRP)
    suspend fun removeGrpUser(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.UPDATE_GRP_DES)
    suspend fun updateGrpDes(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_PRIVATE_INFO)
    suspend fun getPrivateInfo(@Body inputParams: InputParams): GetPrivateInfo

    @POST(ApiUrl.SET_SIGNATURE)
    suspend fun setSignature(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.SET_STAMP)
    suspend fun setStamp(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.NEW_LETTER_SENT)
    suspend fun newLetter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_SIGNATURE_STAMP)
    suspend fun getStampSignature(@Body inputParams: InputParams): GetStampSignature

    @POST(ApiUrl.GET_LETTER)
    suspend fun getLetter(@Body inputParams: InputParams): LetterDetailsRes

    @POST(ApiUrl.GET_LETTER_SENT)
    suspend fun getLetterSent(@Body inputParams: InputParams): SentLetterDetailsRes

    @POST(ApiUrl.VIEW_LETTER)
    suspend fun viewLetter(@Body inputParams: InputParams): ViewLetterDetails

    @POST(ApiUrl.GET_CATEGORY)
    suspend fun getCategory(@Body inputParams: InputParams): ProductCateRes

    @POST(ApiUrl.GET_CLOUD)
    suspend fun getCloud(@Body inputParams: InputParams): CloudRes

    @POST(ApiUrl.FORWARD_LETTER)
    suspend fun forwardLetter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.CREATE_FOLDER_CREATE_CLOUD)
    suspend fun createCloudFolder(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.CREATE_SUB_FOLDER_CREATE_CLOUD)
    suspend fun createCloudSubFolder(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.GET_CLOUD_DETAILS)
    suspend fun getCloudDetails(@Body inputParams: InputParams): CloudDetailsRes

    @POST(ApiUrl.GET_FILE_CLOUD)
    suspend fun getCloudFile(@Body inputParams: InputParams): GetCloudFileRes

    @POST(ApiUrl.GET_LETTER_HEADER)
    suspend fun getLetterHeader(@Body inputParams: InputParams): GetLetterHeaderFooterRes

    @POST(ApiUrl.GET_LETTER_FOOTER)
    suspend fun getLetterFooter(@Body inputParams: InputParams): GetLetterHeaderFooterRes

    @POST(ApiUrl.SET_LETTER_HEADER)
    suspend fun setLetterHeader(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.SET_LETTER_FOOTER)
    suspend fun setLetterFooter(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.CHANGE_GRP_DETAILS)
    suspend fun changeGrpDetails(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.EXIT_GRP)
    suspend fun grpExit(@Body inputParams: InputParams): BaseResponse

    @POST(ApiUrl.ADD_GRP_ADMIN)
    suspend fun addGrpAdmin(@Body inputParams: InputParams): BaseResponse


    @Multipart
    @POST(ApiUrl.SIGNATURE_UPLOAD)
    suspend fun uploadSignature(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("name") group_name: RequestBody,
        @Part signature: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.LETTER_HEADER_UPLOAD)
    suspend fun uploadLetterHeader(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("name") group_name: RequestBody,
        @Part header: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.LETTER_FOOTER_UPLOAD)
    suspend fun uploadLetterFooter(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("name") group_name: RequestBody,
        @Part footer: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.STAMP_UPLOAD)
    suspend fun stampUpload(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("name") group_name: RequestBody,
        @Part stamp: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.CREATE_GRP)
    suspend fun grpCreate(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("group_name") group_name: RequestBody,
        @Part("members") members: RequestBody,
        @Part group_profile: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.FILE_UPLOAD)
    suspend fun fileUpload(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part file: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.COMPOSE_MAIL)
    suspend fun composeMail(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("to_mail") to_mail: RequestBody,
        @Part("cc_mail") cc_mail: RequestBody,
        @Part("bcc_mail") bcc_mail: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part("body") body: RequestBody,
        @Part attachment: List<MultipartBody.Part?>,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.UPLOAD_FILE_CLOUD)
    suspend fun fileUploadCloud(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("parent_folder_id") parent_folder_id: RequestBody,
        @Part("access_period") access_period: RequestBody,
        @Part("period_limit") period_limit: RequestBody,
        @Part("file_type") file_type: RequestBody,
        @Part file: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.UPLOAD_FILE_CLOUD)
    suspend fun fileUploadCloudFolder(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("subparent_folder_id") subparent_folder_id: RequestBody,
        @Part("access_period") access_period: RequestBody,
        @Part("period_limit") period_limit: RequestBody,
        @Part("file_type") file_type: RequestBody,
        @Part file: MultipartBody.Part,
    ): BaseResponse

    @Multipart
    @POST(ApiUrl.CHANGE_GRP_PROFILE)
    suspend fun changeGrpProfile(
        @Part("user_id") user_id: RequestBody,
        @Part("accessToken") accessToken: RequestBody,
        @Part("group_id") subparent_folder_id: RequestBody,
        @Part group_profile: MultipartBody.Part,
    ): BaseResponse


}