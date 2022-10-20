package com.application.smartstation.ui.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng

class LoginResponse(var status: Boolean, var message: String, var otp: Int, var statuscode: Int)
class OTPVerifyResponse(var status: Boolean, var message: String, var data: DataOtp)
class DataOtp(
    var id: String,
    var name: String,
    var email: String,
    var about: String,
    var phone: String,
    var login_status: String,
    var company_name: String,
    var company_mail: String,
    var country: String,
    var profile_pic: String,
    var deviceType: String,
    var deviceToken: String,
    var accessToken: String,
    var status: String,
    var security_status: String,
)

class ProfileUpdateResponse(
    var status: Boolean,
    var message: String,
    var user_id: String,
    var data: DataProfile,
)

class DataProfile(
    var id: String,
    var name: String,
    var phone: String,
    var company_name: String,
    var company_mail: String,
    var country: String,
    var profile_pic: String,
    var deviceType: String,
    var deviceToken: String,
    var accessToken: String,
    var status: String,
    var email: String,
    var about: String,
)

class ImageUploadResponse(var status: Boolean, var imageurl: String, var message: String)
class TermsResponse(
    var status: Boolean,
    var imageurl: String,
    var message: String,
    var terms: String,
)

class ChangeNotification(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var notification_status: String,
    var sound: String,
    var vibration: String,
)

class BaseResponse(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var filepath: String,
    var cloud_id: String,
    var privacy_status: String,
    var end_time: String,
)

class UpdateProfilePicResponse(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var profile_pic: String,
)

class EditProfileResponse(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var name: String,
    var email: String,
    var about: String,
)

class GetSignatureResponse(
    var status: Boolean,
    var message: String,
    var data: ArrayList<SignatureListData>,
)

class SignatureListData(var id: String, var name: String, var image: String, var default: Boolean)
class GetTFAResponse(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var secret_key: String,
    var tfa_status: String,
)

class GetChatListResponse(
    var status: Boolean,
    var message: String,
    var data: ArrayList<DataChatList>,
)

class DataChatList(
    var id: String,
    var date: String,
    var message: String,
    var phone: String,
    var message_type: String,
    var unread_message: String,
    var userid: String,
    var name: String,
    var profile: String,
    var room: String,
    var chat_type: String,
)

class GetMailListResponse(
    var status: Boolean,
    var message: String,
    var data: ArrayList<DataMailList>,
)

class DataMailList(
    var id: String,
    var user_id: String,
    var datetime: String,
    var createdAt: String,
    var to: Array<String>,
    var from: String,
    var bcc: Array<String>,
    var cc: Array<String>,
    var attachments: ArrayList<MailAttachmentList>,
    var subject: String,
    var body: String,
    var mail_read_status: String,
    var profile_pic: String,
    var type: String,
)

class ProfileRes(var profile_pic: String)
class GetUserListResponse(
    var status: Boolean,
    var message: String,
    var data: ArrayList<DataUserList>,
)

class DataUserList(
    var user_id: String,
    var name: String,
    var profile_pic: String,
    var phone: String,
    var country: String,
    var about: String,
    var statusSelected: Boolean = false,
    var alreadySelected: Boolean = false,
)

class GetChatDetailsListResponse(var status: Boolean, var message: String, var data: ChatListRes)
class ChatListRes(var group_name:String,var group_profile:String,var id:String,var user_left_status:String,var user_block_status:Int,var list: ArrayList<ChatDetailsRes>)
class ChatDetailsRes(
    var id: String,
    var date: String,
    var senter_id: String,
    var receiver_id: String,
    var message: String,
    var message_type: String,
    var message_status: String,
    var room: String,
    var type: String,
    var name: String,
)

class OnlineRes(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var online_status: String,
    var last_seen: String,
)

class TypingRes(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var typing: String,
    var user_id: String,
    var name: String,
)

class GrpUserListRes(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var group_name: String,
    var created_datetime: String,
    var group_profile: String,
    var number_of_members: String,
    var description: String,
    var description_updated_datetime: String,
    var media_count: Int,
    var data: ArrayList<UserListGrp>,
)

class UserListGrp(
    var user_id: String,
    var username: String,
    var type: String,
    var about: String,
    var phone: String,
    var profile_pic: String,
)

class SendMailRes(
    var status: Boolean,
    var message: String,
    var statuscode: String,
    var data: ArrayList<SendMailListRes>,
)

class SendMailListRes(
    var id: String,
    var from: String,
    var userId: String,
    var attachments: ArrayList<MailAttachmentList>,
    var createdAt: String,
    var body: String,
    var subject: String,
    var inboxId: String,
    var to: Array<String>,
    var datetime: String,
    var bcc: Array<String>,
    var cc: Array<String>,
    var bodyMD5Hash: String,
    var virtualSend: String,
    var profile_pic: String,
    var type: String,
)

class MailImageSelect(var imagePath: String, var type: String)
class MailAttachmentList(var attachment: String)
class InboxDetailsRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: DataInbox,
)

class DataInbox(
    var from: String,
    var to: Array<String>,
    var cc: Array<String>,
    var bcc: Array<String>,
    var replyTo: String,
    var created_datetime: String,
    var subject: String,
    var body: String,
    var attachments: Array<String>,
    var sent_datetime: String,
)

class GetStampRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: ArrayList<StampList>,
)

class StampList(
    var id: String,
    var user_id: String,
    var name: String,
    var image: String,
    var default: Boolean,
)

class GetStampSignature(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: SignatureStampRes,
)

class SignatureStampRes(
    var default_signature: String,
    var default_stamp: String,
    var signature: ArrayList<SignatureListData>,
    var stamp: ArrayList<StampList>,
    var header: ArrayList<ListHeaderFooterRes>,
    var footer: ArrayList<ListHeaderFooterRes>,
)

class CloudRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var cloud_numbers: ArrayList<CloudListRes>,
)

class CloudListRes(
    var phone: String,
    var profile_pic: String,
    var id: String,
    var folder_name: String,
    var user_id: String,
)

class ProductCateRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: ArrayList<ProductCateListRes>,
)

class ProductCateListRes(
    var id: Int,
    var category_id: String,
    var category_name: String,
    var category_image: String,
)

class LetterDetailsRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: DataLetterRes,
)

class DataLetterRes(var unread: Int, var letter_list: ArrayList<DataLetter>)
class DataLetter(
    var id: String,
    var user_id: String,
    var from: String,
    var to: Array<String>,
    var cc: Array<String>,
    var bcc: Array<String>,
    var datetime: String,
    var subject: String,
    var body: String,
    var letter_path: String,
    var profile_pic: String,
    var mail_read_status: String,
    var type: String,
)

class SentLetterDetailsRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: DataSentLetterRes,
)

class DataSentLetterRes(var letter_sent_list: ArrayList<DataSentLetter>)
class DataSentLetter(
    var id: String,
    var user_id: String,
    var from: String,
    var to: Array<String>,
    var cc: Array<String>,
    var bcc: Array<String>,
    var datetime: String,
    var subject: String,
    var body: String,
    var letter_path: String,
    var profile_pic: String,
    var type: String,
)

class ViewLetterDetails(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var data: DataSentLetter,
)

class CloudDetailsRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var send_datas: ArrayList<CloudDetailListRes>,
    var received_datas: ArrayList<CloudDetailListRes>,
)

class CloudDetailListRes(
    var id: String,
    var user_id: String,
    var cloud_upload_id: String,
    var parent_folder_id: String,
    var created_datetime: String,
    var file_type: String,
    var name: String,
    var file_path: String,
    var receiver_id: String,
    var view_type: String,
    var time_period: String,
    var end_datetime: String,
    var status: String,
)

class ContactListRes(var name: String, var Phn: String)

class GetCloudFileRes(
    var status: Boolean,
    var statuscode: Int,
    var message: String,
    var datas: ArrayList<CloudDetailListRes>,
)

class GetLetterHeaderFooterRes(var status: Boolean, var message: String, var statuscode:Int, var data:ArrayList<ListHeaderFooterRes>)
class ListHeaderFooterRes(var id:String, var user_id: String, var name: String, var image: String,var default:Boolean)

class GetPrivateInfo(var status: Boolean, var message: String, var statuscode:Int, var data:PrivateInfoRes)
class PrivateInfoRes(var receiver_data:DataReceiverResInfo, var common_group_data:CommonGrpData, var media_count:Int, var user_block_status:Int)
class CommonGrpData(var no_of_groups:Int, var data:ArrayList<CommonGrpList>)
class CommonGrpList(var group_id:String, var group_name:String, var group_profile_pic:String, var group_users:String)
class DataReceiverResInfo(var name:String, var profile_pic:String, var phone:String, var about:String, var about_updated_datetime:String)
class GetMedia(var status: Boolean, var message: String, var statuscode:Int, var data:DataMediaRes)
class DataMediaRes(var medias:ArrayList<MediaRes>,var docs:ArrayList<MediaRes>,var links:ArrayList<MediaRes>)
class MediaRes(var id:String, var user_id:String, var username:String, var date:String, var path:String, var type:String)



//un use
class SendMsgResponse(var status: Boolean, var message: String)
class PostResponse(var ip: String)
class CountryList(var countryCode: String, var countryNum: String, var countryName: String)
class Person(var name: String, var email: String)
class ChatResponse(
    var title: String,
    var sub: String,
    var time: String,
    var date: String,
    var profile: Int,
    var status: Int,
)

class ChatHisResponse(var data: ArrayList<DataResChat>)
class DataResChat(
    var date: String,
    var type: String,
    var msgType: String,
    var msg: String,
    var time: String,
    var imgUrl: Int,
    var readStatus: Int,
)

fun Location.toLatLng(): LatLng = LatLng(this.latitude, this.longitude)
fun LatLng.toLatLngString(): String = "${this.latitude},${this.longitude}"

