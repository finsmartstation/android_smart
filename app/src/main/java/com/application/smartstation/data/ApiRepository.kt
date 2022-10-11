package com.application.smartstation.data

import com.application.smartstation.service.ApiService
import com.application.smartstation.ui.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ApiRepository @Inject constructor(val apiService: ApiService) {

    fun login(inputParams: InputParams): Flow<LoginResponse> {
        return flow {
            val response = apiService.login(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun otpVerify(inputParams: InputParams): Flow<OTPVerifyResponse> {
        return flow {
            val response = apiService.otpVerify(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun resendOTP(inputParams: InputParams): Flow<LoginResponse> {
        return flow {
            val response = apiService.resendOTP(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun updateProfile(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        profile_pic: MultipartBody.Part,
    ): Flow<ProfileUpdateResponse> {
        return flow {
            val response = apiService.updateProfile(user_id, accessToken, name, profile_pic)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun imgUpload(image: MultipartBody.Part): Flow<ImageUploadResponse> {
        return flow {
            val response = apiService.imgUpload(image)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getTerms(): Flow<TermsResponse> {
        return flow {
            val response = apiService.getTerms()
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun logout(inputParams: InputParams): Flow<LoginResponse> {
        return flow {
            val response = apiService.logout(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun changeNotification(inputParams: InputParams): Flow<ChangeNotification> {
        return flow {
            val response = apiService.changeNotification(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getNotification(inputParams: InputParams): Flow<ChangeNotification> {
        return flow {
            val response = apiService.getNotification(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun sendMailOTP(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.sendMailOTP(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun updateProfilePic(
        user_id: RequestBody,
        accessToken: RequestBody,
        profile_pic: MultipartBody.Part,
    ): Flow<UpdateProfilePicResponse> {
        return flow {
            val response = apiService.updateProfilePic(user_id, accessToken, profile_pic)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun editProfile(inputParams: InputParams): Flow<EditProfileResponse> {
        return flow {
            val response = apiService.editProfile(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getProfile(inputParams: InputParams): Flow<ProfileUpdateResponse> {
        return flow {
            val response = apiService.getProfile(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun passcodeUpdate(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.passcodeUpdate(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun passcodeCheck(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.passcodeCheck(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun passcodeRemove(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.passcodeRemove(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun uploadSignature(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        signature: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.uploadSignature(user_id, accessToken, name, signature)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun uploadLetterHeader(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        header: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.uploadLetterHeader(user_id, accessToken, name, header)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun uploadLetterFooter(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        footer: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.uploadLetterFooter(user_id, accessToken, name, footer)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun stampUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        stamp: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.stampUpload(user_id, accessToken, name, stamp)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getSignature(inputParams: InputParams): Flow<GetSignatureResponse> {
        return flow {
            val response = apiService.getSignature(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun removeSignature(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.removeSignature(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun checkEmailOTP(inputParams: InputParams): Flow<OTPVerifyResponse> {
        return flow {
            val response = apiService.checkEmailOTP(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun resendEmailOTP(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.resendEmailOTP(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getTFA(inputParams: InputParams): Flow<GetTFAResponse> {
        return flow {
            val response = apiService.getTFA(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun updateTFA(inputParams: InputParams): Flow<GetTFAResponse> {
        return flow {
            val response = apiService.updateTFA(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getChatlist(inputParams: InputParams): Flow<GetChatListResponse> {
        return flow {
            val response = apiService.getChatlist(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getMaillist(inputParams: InputParams): Flow<GetMailListResponse> {
        return flow {
            val response = apiService.getMaillist(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getUserlist(inputParams: InputParams): Flow<GetUserListResponse> {
        return flow {
            val response = apiService.getUserlist(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getChatDetailslist(inputParams: InputParams): Flow<GetChatDetailsListResponse> {
        return flow {
            val response = apiService.getChatDetailslist(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getGrpDetails(inputParams: InputParams): Flow<GetChatDetailsListResponse> {
        return flow {
            val response = apiService.getGrpDetails(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getGrpUserList(inputParams: InputParams): Flow<GrpUserListRes> {
        return flow {
            val response = apiService.getGrpUserList(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun addGrpUser(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.addGrpUser(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun composeMailWithoutImage(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.composeMailWithoutImage(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun sendMail(inputParams: InputParams): Flow<SendMailRes> {
        return flow {
            val response = apiService.sendMail(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getInbox(inputParams: InputParams): Flow<InboxDetailsRes> {
        return flow {
            val response = apiService.getInbox(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getSent(inputParams: InputParams): Flow<InboxDetailsRes> {
        return flow {
            val response = apiService.getSent(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun deleteMail(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.deleteMail(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun deleteLetter(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.deleteLetter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getStamp(inputParams: InputParams): Flow<GetStampRes> {
        return flow {
            val response = apiService.getStamp(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun removeStamp(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.removeStamp(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun removeHeader(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.removeHeader(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

  fun removeFooter(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.removeFooter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getPrivateInfo(inputParams: InputParams): Flow<GetPrivateInfo> {
        return flow {
            val response = apiService.getPrivateInfo(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun setSignature(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.setSignature(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun setStamp(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.setStamp(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun newLetter(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.newLetter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getStampSignature(inputParams: InputParams): Flow<GetStampSignature> {
        return flow {
            val response = apiService.getStampSignature(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getLetter(inputParams: InputParams): Flow<LetterDetailsRes> {
        return flow {
            val response = apiService.getLetter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getLetterSent(inputParams: InputParams): Flow<SentLetterDetailsRes> {
        return flow {
            val response = apiService.getLetterSent(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun viewLetter(inputParams: InputParams): Flow<ViewLetterDetails> {
        return flow {
            val response = apiService.viewLetter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getCategory(inputParams: InputParams): Flow<ProductCateRes> {
        return flow {
            val response = apiService.getCategory(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getCloud(inputParams: InputParams): Flow<CloudRes> {
        return flow {
            val response = apiService.getCloud(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun forwardLetter(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.forwardLetter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun createCloudFolder(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.createCloudFolder(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun createCloudSubFolder(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.createCloudSubFolder(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getCloudDetails(inputParams: InputParams): Flow<CloudDetailsRes> {
        return flow {
            val response = apiService.getCloudDetails(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getCloudFile(inputParams: InputParams): Flow<GetCloudFileRes> {
        return flow {
            val response = apiService.getCloudFile(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getLetterHeader(inputParams: InputParams): Flow<GetLetterHeaderFooterRes> {
        return flow {
            val response = apiService.getLetterHeader(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun getLetterFooter(inputParams: InputParams): Flow<GetLetterHeaderFooterRes> {
        return flow {
            val response = apiService.getLetterFooter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun setLetterHeader(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.setLetterHeader(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun setLetterFooter(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.setLetterFooter(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun changeGrpDetails(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.changeGrpDetails(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun grpExit(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.grpExit(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun addGrpAdmin(inputParams: InputParams): Flow<BaseResponse> {
        return flow {
            val response = apiService.addGrpAdmin(inputParams)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }


    fun grpCreate(
        user_id: RequestBody,
        accessToken: RequestBody,
        group_name: RequestBody,
        members: RequestBody,
        group_profile: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response =
                apiService.grpCreate(user_id, accessToken, group_name, members, group_profile)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun fileUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        file: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.fileUpload(user_id, accessToken, file)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun composeMail(
        user_id: RequestBody,
        accessToken: RequestBody,
        to_mail: RequestBody,
        cc_mail: RequestBody,
        bcc_mail: RequestBody,
        subject: RequestBody,
        body: RequestBody,
        attachment: List<MultipartBody.Part?>,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.composeMail(user_id,
                accessToken,
                to_mail,
                cc_mail,
                bcc_mail,
                subject,
                body,
                attachment)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun fileUploadCloud(
        user_id: RequestBody,
        accessToken: RequestBody,
        parent_folder_id: RequestBody,
        access_period: RequestBody,
        period_limit: RequestBody,
        file_type: RequestBody,
        file: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.fileUploadCloud(user_id,
                accessToken,
                parent_folder_id,
                access_period,
                period_limit,
                file_type,
                file)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun fileUploadCloudFolder(
        user_id: RequestBody,
        accessToken: RequestBody,
        subparent_folder_id: RequestBody,
        access_period: RequestBody,
        period_limit: RequestBody,
        file_type: RequestBody,
        file: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.fileUploadCloudFolder(user_id,
                accessToken,
                subparent_folder_id,
                access_period,
                period_limit,
                file_type,
                file)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun changeGrpProfile(
        user_id: RequestBody,
        accessToken: RequestBody,
        group_id: RequestBody,
        group_profile: MultipartBody.Part,
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.changeGrpProfile(user_id,
                accessToken,
                group_id,
                group_profile)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

}