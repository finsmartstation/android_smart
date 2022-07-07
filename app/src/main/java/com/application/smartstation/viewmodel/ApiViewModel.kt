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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.collect
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(
    application: Application,
    private val repository: ApiRepository
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

    fun updateProfile(user_id: RequestBody, accessToken: RequestBody, name: RequestBody, profile_pic: MultipartBody.Part) = liveData<Resource<ProfileUpdateResponse>> {
        if (UtilsDefault.isOnline()){
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

    fun updateProfilePic(user_id: RequestBody, accessToken: RequestBody, profile_pic: MultipartBody.Part) = liveData<Resource<UpdateProfilePicResponse>> {
        if (UtilsDefault.isOnline()){
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

    fun uploadSignature(inputParams: InputParams) = liveData<Resource<BaseResponse>> {
        if (UtilsDefault.isOnline()) {
            repository.uploadSignature(inputParams)
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

    fun getChatDetailslist(inputParams: InputParams) = liveData<Resource<GetChatDetailsListResponse>> {
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

    fun fileUpload(user_id:RequestBody,accessToken:RequestBody,file: MultipartBody.Part) = liveData<Resource<BaseResponse>> {
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

}