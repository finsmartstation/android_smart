package com.application.smartstation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.application.smartstation.data.ApiRepository
import com.application.smartstation.service.Resource
import com.application.smartstation.ui.model.*
import com.application.smartstation.util.UtilsDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(
    application: Application,
    private val repository: ApiRepository,
) : AndroidViewModel(application) {

    fun login(inputParams: InputParams) = liveData<Resource<LoginResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.login(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun otpVerify(inputParams: InputParams) = liveData<Resource<OTPVerifyResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.otpVerify(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun resendOTP(inputParams: InputParams) = liveData<Resource<LoginResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.resendOTP(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun updateProfile(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        profile_pic: MultipartBody.Part,
    ) = liveData<Resource<ProfileUpdateResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.updateProfile(user_id, accessToken, name, profile_pic)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun imgUpload(image: MultipartBody.Part) = liveData<Resource<ImageUploadResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.imgUpload(image)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getTerms() = liveData<Resource<TermsResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getTerms()
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun logout(inputParams: InputParams) = liveData<Resource<LoginResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.logout(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun changeNotification(inputParams: InputParams) = liveData<Resource<ChangeNotification>> {
        if (UtilsDefault.isOnline()) {
            repository.changeNotification(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getNotification(inputParams: InputParams) = liveData<Resource<ChangeNotification>> {
        if (UtilsDefault.isOnline()) {
            repository.getNotification(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun sendMailOTP(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.sendMailOTP(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun updateProfilePic(
        user_id: RequestBody,
        accessToken: RequestBody,
        profile_pic: MultipartBody.Part,
    ) = liveData<Resource<UpdateProfilePicResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.updateProfilePic(user_id, accessToken, profile_pic)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun editProfile(inputParams: InputParams) = liveData<Resource<EditProfileResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.editProfile(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getProfile(inputParams: InputParams) = liveData<Resource<ProfileUpdateResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getProfile(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun passcodeUpdate(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.passcodeUpdate(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun passcodeCheck(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.passcodeCheck(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun passcodeRemove(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.passcodeRemove(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun uploadSignature(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        signature: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.uploadSignature(user_id, accessToken, name, signature)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun uploadLetterHeader(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        header: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.uploadLetterHeader(user_id, accessToken, name, header)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun uploadLetterFooter(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        footer: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.uploadLetterFooter(user_id, accessToken, name, footer)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun stampUpload(
        user_id: RequestBody,
        accessToken: RequestBody,
        name: RequestBody,
        stamp: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.stampUpload(user_id, accessToken, name, stamp)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getSignature(inputParams: InputParams) = liveData<Resource<GetSignatureResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getSignature(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun removeSignature(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.removeSignature(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun checkEmailOTP(inputParams: InputParams) = liveData<Resource<OTPVerifyResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.checkEmailOTP(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun resendEmailOTP(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.resendEmailOTP(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getTFA(inputParams: InputParams) = liveData<Resource<GetTFAResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getTFA(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun updateTFA(inputParams: InputParams) = liveData<Resource<GetTFAResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.updateTFA(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getChatlist(inputParams: InputParams) = liveData<Resource<GetChatListResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getChatlist(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getMaillist(inputParams: InputParams) = liveData<Resource<GetMailListResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getMaillist(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getUserlist(inputParams: InputParams) = liveData<Resource<GetUserListResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getUserlist(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getChatDetailslist(inputParams: InputParams) =
        liveData<Resource<GetChatDetailsListResponse>> {
            if (UtilsDefault.isOnline()) {
                repository.getChatDetailslist(inputParams)
                    .onStart {
                        emit(Resource.loading(data = null))
                    }
                    .catch {
                        emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                    }
                    .collect {
                        emit(Resource.success(it))
                    }
            } else {
                emit(Resource.error(data = null, msg = "No internet connection"))
            }

        }

    fun getGrpDetails(inputParams: InputParams) = liveData<Resource<GetChatDetailsListResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getGrpDetails(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getGrpUserList(inputParams: InputParams) = liveData<Resource<GrpUserListRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getGrpUserList(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun addGrpUser(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.addGrpUser(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun composeMailWithoutImage(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.composeMailWithoutImage(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun sendMail(inputParams: InputParams) = liveData<Resource<SendMailRes>> {
        if (UtilsDefault.isOnline()) {
            repository.sendMail(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getInbox(inputParams: InputParams) = liveData<Resource<InboxDetailsRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getInbox(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getSent(inputParams: InputParams) = liveData<Resource<InboxDetailsRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getSent(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun deleteMail(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.deleteMail(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun deleteLetter(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.deleteLetter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getStamp(inputParams: InputParams) = liveData<Resource<GetStampRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getStamp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun removeStamp(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.removeStamp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun removeHeader(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.removeHeader(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun removeFooter(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.removeFooter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun userBlock(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.userBlock(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun userUnblock(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.userUnblock(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun removeGrpUser(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.removeGrpUser(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun updateGrpDes(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.updateGrpDes(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun changePublicProfile(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.changePublicProfile(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getPublicProfile(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.getPublicProfile(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun chatClear(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.chatClear(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun chatClearGrp(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.chatClearGrp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun muteNoficitaionPrivate(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.muteNoficitaionPrivate(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun unmuteNoficitaionPrivate(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.unmuteNoficitaionPrivate(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun muteNotificaionGrp(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.muteNotificaionGrp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun unmuteNotificaionGrp(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.unmuteNotificaionGrp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun privateReportChat(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.privateReportChat(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun grpReportChat(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.grpReportChat(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun privateReportandBlkChat(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.privateReportandBlkChat(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun grpReportandBlkChat(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.grpReportandBlkChat(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getGrpMedia(inputParams: InputParams) = liveData<Resource<GetMedia>> {
        if (UtilsDefault.isOnline()) {
            repository.getGrpMedia(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getPrivateMedia(inputParams: InputParams) = liveData<Resource<GetMedia>> {
        if (UtilsDefault.isOnline()) {
            repository.getPrivateMedia(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getPrivateInfo(inputParams: InputParams) = liveData<Resource<GetPrivateInfo>> {
        if (UtilsDefault.isOnline()) {
            repository.getPrivateInfo(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun setSignature(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.setSignature(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun setStamp(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.setStamp(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun newLetter(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.newLetter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getStampSignature(inputParams: InputParams) = liveData<Resource<GetStampSignature>> {
        if (UtilsDefault.isOnline()) {
            repository.getStampSignature(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getLetter(inputParams: InputParams) = liveData<Resource<LetterDetailsRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getLetter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getLetterSent(inputParams: InputParams) = liveData<Resource<SentLetterDetailsRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getLetterSent(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun viewLetter(inputParams: InputParams) = liveData<Resource<ViewLetterDetails>> {
        if (UtilsDefault.isOnline()) {
            repository.viewLetter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getCategory(inputParams: InputParams) = liveData<Resource<ProductCateRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getCategory(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getCloud(inputParams: InputParams) = liveData<Resource<CloudRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getCloud(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun forwardLetter(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.forwardLetter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun createCloudFolder(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.createCloudFolder(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }


    fun createCloudSubFolder(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.createCloudSubFolder(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getCloudDetails(inputParams: InputParams) = liveData<Resource<CloudDetailsRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getCloudDetails(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getCloudFile(inputParams: InputParams) = liveData<Resource<GetCloudFileRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getCloudFile(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getLetterHeader(inputParams: InputParams) = liveData<Resource<GetLetterHeaderFooterRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getLetterHeader(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun getLetterFooter(inputParams: InputParams) = liveData<Resource<GetLetterHeaderFooterRes>> {
        if (UtilsDefault.isOnline()) {
            repository.getLetterFooter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun setLetterHeader(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.setLetterHeader(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun setLetterFooter(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.setLetterFooter(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun changeGrpDetails(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.changeGrpDetails(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun grpExit(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.grpExit(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun addGrpAdmin(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.addGrpAdmin(inputParams)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun grpCreate(
        user_id: RequestBody,
        accessToken: RequestBody,
        group_name: RequestBody,
        members: RequestBody,
        group_profile: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.grpCreate(user_id, accessToken, group_name, members, group_profile)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun fileUpload(user_id: RequestBody, accessToken: RequestBody, file: MultipartBody.Part) =
        liveData<Resource<BaseResponse>> {
            if (UtilsDefault.isOnline()) {
                repository.fileUpload(user_id, accessToken, file)
                    .onStart {
                        emit(Resource.loading(data = null))
                    }
                    .catch {
                        emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                    }
                    .collect {
                        emit(Resource.success(it))
                    }
            } else {
                emit(Resource.error(data = null, msg = "No internet connection"))
            }

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
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.composeMail(user_id,
                accessToken,
                to_mail,
                cc_mail,
                bcc_mail,
                subject,
                body,
                attachment)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun fileUploadCloud(
        user_id: RequestBody,
        accessToken: RequestBody,
        parent_folder_id: RequestBody,
        access_period: RequestBody,
        period_limit: RequestBody,
        file_type: RequestBody,
        file: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.fileUploadCloud(user_id,
                accessToken,
                parent_folder_id,
                access_period,
                period_limit,
                file_type,
                file)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun fileUploadCloudFolder(
        user_id: RequestBody,
        accessToken: RequestBody,
        subparent_folder_id: RequestBody,
        access_period: RequestBody,
        period_limit: RequestBody,
        file_type: RequestBody,
        file: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.fileUploadCloudFolder(user_id,
                accessToken,
                subparent_folder_id,
                access_period,
                period_limit,
                file_type,
                file)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

    fun changeGrpProfile(
        user_id: RequestBody,
        accessToken: RequestBody,
        group_id: RequestBody,
        group_profile: MultipartBody.Part,
    ) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.changeGrpProfile(user_id,
                accessToken,
                group_id,
                group_profile)
                .onStart {
                    emit(Resource.loading(data = null))
                }
                .catch {
                    emit(Resource.error(data = null, msg = "Cannot reach server..try again"))
                }
                .collect {
                    emit(Resource.success(it))
                }
        } else {
            emit(Resource.error(data = null, msg = "No internet connection"))
        }

    }

}