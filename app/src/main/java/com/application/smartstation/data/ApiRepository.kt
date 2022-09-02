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

    fun updateProfile(user_id:RequestBody,accessToken:RequestBody,name:RequestBody,profile_pic: MultipartBody.Part): Flow<ProfileUpdateResponse> {
        return flow {
            val response = apiService.updateProfile(user_id,accessToken,name,profile_pic)
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

    fun updateProfilePic(user_id:RequestBody,accessToken:RequestBody,profile_pic: MultipartBody.Part): Flow<UpdateProfilePicResponse> {
        return flow {
            val response = apiService.updateProfilePic(user_id,accessToken,profile_pic)
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

    fun uploadSignature(user_id:RequestBody,accessToken:RequestBody,name:RequestBody,signature: MultipartBody.Part): Flow<BaseResponse> {
        return flow {
            val response = apiService.uploadSignature(user_id,accessToken,name,signature)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun stampUpload(user_id:RequestBody,accessToken:RequestBody,name:RequestBody,stamp: MultipartBody.Part): Flow<BaseResponse> {
        return flow {
            val response = apiService.stampUpload(user_id,accessToken,name,stamp)
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

    fun grpCreate(
        user_id:RequestBody,
        accessToken:RequestBody,
        group_name:RequestBody,
        members:RequestBody,
        group_profile: MultipartBody.Part): Flow<BaseResponse> {
        return flow {
            val response = apiService.grpCreate(user_id, accessToken, group_name, members, group_profile)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun fileUpload(user_id:RequestBody,accessToken:RequestBody,file: MultipartBody.Part): Flow<BaseResponse> {
        return flow {
            val response = apiService.fileUpload(user_id,accessToken, file)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

    fun composeMail(
        user_id:RequestBody,
        accessToken:RequestBody,
        to_mail:RequestBody,
        cc_mail:RequestBody,
        bcc_mail:RequestBody,
        subject:RequestBody,
        body:RequestBody,
        attachment: List<MultipartBody.Part?>
    ): Flow<BaseResponse> {
        return flow {
            val response = apiService.composeMail(user_id,accessToken, to_mail, cc_mail, bcc_mail, subject, body, attachment)
            emit(response)
        }.flowOn(Dispatchers.IO)
    }

}