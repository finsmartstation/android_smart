package com.application.smartstation.util


import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.application.smartstation.R
import com.application.smartstation.app.App
import com.application.smartstation.service.MailCallback
import com.application.smartstation.service.background.SocketService
import com.application.smartstation.ui.activity.MainActivity
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object UtilsDefault {

    internal var letter = Pattern.compile("[a-zA-z]")
    internal var digit = Pattern.compile("[0-9]")

    fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "SMART_STATION_CHANNEL_ID"
        val channelName = "SMART_STATION"


        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(checkNull(title))
            .setContentText(checkNull(message))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)

            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = "smartstationchannel"
            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(channelId)
        }
        val notificationID = (Date().time / 1000L % Integer.MAX_VALUE).toInt()
        notificationManager.notify(notificationID, notificationBuilder.build())


    }


    fun copyToClipboard(context: Context, text: CharSequence) {
        var clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    fun pastefromClipboard(context: Context): String {
        val clipBoardManager =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipBoardManager.primaryClip?.getItemAt(0)?.text.toString()
        return text
    }


    private val SHARED_PREFERENCE_UTILS = "smartstation"
    private val FCMKEYSHAREDPERFRENCES = "Fcmpreference"

    private var sharedPreferences: SharedPreferences? = null
    private var sharedPreferencesfcm: SharedPreferences? = null

    private fun initializeSharedPreference() {

        sharedPreferences = App.instance.getSharedPreferences(
            SHARED_PREFERENCE_UTILS,
            Context.MODE_PRIVATE
        )
    }

    private fun initializeSharedPreferencefcm() {

        sharedPreferencesfcm = App.instance.getSharedPreferences(
            FCMKEYSHAREDPERFRENCES,
            Context.MODE_PRIVATE
        )
    }

    fun updateSharedPreferenceFCM(key: String, value: String) {
        if (sharedPreferencesfcm == null) {
            initializeSharedPreferencefcm()
        }

        val editor = sharedPreferencesfcm!!.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun updateSharedPreferenceString(key: String, value: String) {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }

        val editor = sharedPreferences!!.edit()
        editor.putString(key, value)
        editor.commit()
    }


    fun updateSharedPreferenceInt(key: String, value: Int) {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }

        val editor = sharedPreferences!!.edit()
        editor.putInt(key, value)
        editor.commit()
    }


    fun updateSharedPreferenceBoolean(key: String, value: Boolean) {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }

        val editor = sharedPreferences!!.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }


    fun setLoggedIn(context: Context, value: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val ed = sp.edit()
        ed.putBoolean(Constants.LOGIN_STATUS, value)
        ed.commit()
    }

    fun isLoggedIn(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(Constants.LOGIN_STATUS, false)
    }

    fun getSharedPreferenceValuefcm(key: String?): String? {
        if (sharedPreferencesfcm == null) {
            initializeSharedPreferencefcm()
        }

        return if (key != null) {
            sharedPreferencesfcm!!.getString(key, null)
        } else {
            null
        }
    }

    fun getSharedPreferenceString(key: String?): String? {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }

        return if (key != null) {
            sharedPreferences!!.getString(key, null)
        } else {
            null
        }
    }

    fun isAppInstalled(context: Context, packagename: String): Boolean {

        val pm = context.packageManager
        try {
            val ps = pm.getPackageInfo(packagename, 0)
            Log.d("TAG", "isAppInstalled: " + ps.versionName)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getSharedPreferenceInt(key: String?): Int {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }
        return if (key != null) {
            sharedPreferences!!.getInt(key, -1)
        } else {
            0
        }
    }

    fun getSharedPreferenceBoolean(key: String?): Boolean {
        if (sharedPreferences == null) {
            initializeSharedPreference()
        }
        return key != null && sharedPreferences!!.getBoolean(key, false)
    }


    fun checkNull(data: String?): String {
        return data ?: ""
    }

    fun ok(password: String): Boolean {
        val hasLetter = letter.matcher(password)
        val hasDigit = digit.matcher(password)
        return hasLetter.find() && hasDigit.find()
    }

    fun printException(exception: Exception) {
        exception.printStackTrace()
    }


    fun isOnline(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false
        val cm =
            App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                val netInfo = cm.activeNetworkInfo
                if (netInfo != null) {
                    return (netInfo.isConnected && (netInfo.type == ConnectivityManager.TYPE_WIFI || netInfo.type == ConnectivityManager.TYPE_MOBILE))
                }
            } else {
                val netInfo = cm.activeNetwork
                if (netInfo != null) {
                    val nc = cm.getNetworkCapabilities(netInfo)

                    return (nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    ))
                }
            }
        }

        return haveConnectedWifi || haveConnectedMobile
    }

    enum class BuildType {
        QA, PROD
    }

    fun convertDecimal(decimal: String): String {

        val numner = decimal
        val add = numner.toDouble()
        val add2 = add.toInt()
        val nu = add2 + 1
        val formatted = String.format("%1$-" + nu + "s", "1").replace(' ', '0')

        return formatted
    }

    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun isEmailValid(email: String): Boolean {

        val ePattern =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = Pattern.compile(ePattern)
        val m = p.matcher(email)
        return m.matches()

    }


    fun mAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun clearSession() {

        if (sharedPreferences == null) {
            initializeSharedPreference()
        }
        sharedPreferences!!.edit().clear().apply()
    }

    fun validate(password: String): Boolean {
        return password
            .matches("(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%&*()_+=|<>?{}\\[\\]~-])".toRegex())
    }

    fun validPassword(password: String): Boolean {

        val pattern: Pattern
        val matcher: Matcher

        val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
        val PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
        // val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z][a-z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()

    }

    fun validateDOB(newDate: Calendar): Boolean {
        val minAdultAge = GregorianCalendar()
        minAdultAge.add(Calendar.YEAR, -18)
        return !minAdultAge.before(newDate)
    }

    fun checkUsername(str: String): Boolean {

        var pattern = Pattern.compile("^[a-zA-Z]+$")
        var matcher = pattern.matcher(str)
        return matcher.matches()
    }

    fun validUrl(str: String): Boolean {
        return Patterns.WEB_URL.matcher(str).matches()
    }


    fun isEmailPassword(email: String): Boolean {

        val flag: Boolean
        val expression = "(?=.*[a-z])(?=.*\\d)"

        val inputStr = email.trim { it <= ' ' }
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(inputStr)

        flag = matcher.matches()
        return flag

    }

    fun md5(s: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest = MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                Log.e("msg", "==$aMessageDigest")
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2)
                    h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }

    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfbyte = b.toInt().ushr(4) and 0x0F
            var two_halfs = 0
            do {
                buf.append(if (0 <= halfbyte && halfbyte <= 9) ('0'.toInt() + halfbyte).toChar() else ('a'.toInt() + (halfbyte - 10)).toChar())
                halfbyte = b.toInt() and 0x0F
            } while (two_halfs++ < 1)
        }
        return buf.toString()
    }


    fun SHA1(str: String): String {
        var digest: MessageDigest? = null
        var input: ByteArray? = null

        try {
            digest = MessageDigest.getInstance("SHA-1")
            digest!!.reset()
            input = digest.digest(str.toByteArray(charset("UTF-8")))

        } catch (e1: NoSuchAlgorithmException) {
            e1.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return convertToHex(input!!)
    }


    fun checkScreensize(context: Activity): Int {
        val dm = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels
        val height = dm.heightPixels
        val dens = dm.densityDpi
        val wi = width.toDouble() / dens.toDouble()
        val hi = height.toDouble() / dens.toDouble()
        val x = Math.pow(wi, 2.0)
        val y = Math.pow(hi, 2.0)
        val screenInches = Math.sqrt(x + y)
        val finalVal = Math.round(screenInches).toDouble()

        return finalVal.toInt()
    }

    fun showKeyboardForFocusedView(activity: Activity){
        val inputManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        if (view != null) {
            inputManager.showSoftInput(view, 0)
        }
    }

    fun hideKeyboardForFocusedView(activity: Activity) {
        val inputManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun firstLetterCapital(text: String): String {
        val upperString = text.substring(0, 1).toUpperCase() + text.substring(1)
        return upperString
    }

    @SuppressLint("SimpleDateFormat")
    fun currentDate(): String {
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
        return df.format(c.time)
    }

    fun epochTime(time: String): String {
        val epoch = time.toLong()
        val date = Date(epoch * 1000L)
        val format = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH)
        val formatted = format.format(date)
        return formatted
    }

    fun epochTime2(time: String): String {
        val d = Date((time.toLong() / 1000) * 1000L)
        val formatted: String = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(d)
        return formatted
    }

    fun voteTime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("dd-mm-yyyy HH:mm")
            date = spf.format(newDate)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun apiTime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
            date = spf.format(newDate)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun viewTime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("MMM dd")
            date = spf.format(newDate)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun replyTime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("MMM dd, yyyy")
            date = spf.format(newDate)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun chatTime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("dd MMM HH:mm")
            date = spf.format(newDate)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun ordertime(time: String): String {
        try {
            var date = time
            var spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val newDate = spf.parse(date)
            spf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            date = spf.format(newDate!!)
            return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(Exception::class)
    fun encrypt(plaintext: ByteArray?, key: SecretKey, IV: ByteArray?): ByteArray? {
        val cipher: Cipher = Cipher.getInstance(Constants.TRANSFORMATION)
        val keySpec = SecretKeySpec(key.encoded, Constants.TRANSFORMATION)
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plaintext)
    }

    fun decrypt(cipherText: ByteArray?, key: SecretKey, IV: ByteArray?): String? {
        try {
            val cipher = Cipher.getInstance(Constants.TRANSFORMATION)
            val keySpec = SecretKeySpec(key.encoded, Constants.TRANSFORMATION)
            val ivSpec = IvParameterSpec(IV)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedText = cipher.doFinal(cipherText)
            return String(decryptedText)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun encoderfun(decval: ByteArray?): String? {
        return Base64.encodeToString(decval, Base64.DEFAULT)
    }

    fun decoderfun(enval: String?): ByteArray? {
        return Base64.decode(enval, Base64.DEFAULT)
    }

    fun countryImg(countryCode: String): String {
        val flagOffset = 0x1F1E6
        val asciiOffset = 0x41

        val country = countryCode

        val firstChar = Character.codePointAt(country, 0) - asciiOffset + flagOffset
        val secondChar = Character.codePointAt(country, 1) - asciiOffset + flagOffset

        val flag = (String(Character.toChars(firstChar))
                + String(Character.toChars(secondChar)))

        return flag
    }

    fun PhoneNumberWithoutCountryCode(phoneNumberWithCountryCode: String): String? { //+91 7698989898
        val compile = Pattern.compile(
            "\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?"
        )
        Log.d("tag", "number::_>" + compile.pattern().toRegex()) //OutPut::7698989898
        return phoneNumberWithCountryCode.replace(compile.pattern().toRegex(), "")
    }

    fun initService(socketService: Class<*>, activity: Activity) {
        if (!isMyServiceRunning(socketService, activity)) {
            activity.startService(Intent(activity, SocketService::class.java))
        }
    }

    private fun isMyServiceRunning(socketService: Class<*>, activity: Activity): Boolean {
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (socketService.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun formatToYesterdayOrToday(date: String?): String? {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
            "Today"
        } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
            "Yesterday"
        } else {
            dateConvert(date)
        }
    }

    fun dateChatList(date: String?): String? {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
            todayDate(date)
        } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
            "Yesterday"
        } else {
            dateConvert(date)
        }
    }


    fun dateMute(date: String?): String? {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)

        return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
            todayDate(date)
        } else {
            dateConvert(date)+" "+todayDate(date)
        }
    }

    fun dateConvert(date: String?): String {
        val readFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val writeFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        var dateVal: Date? = null
        try {
            dateVal = readFormat.parse(date)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (dateVal != null) {
            return writeFormat.format(dateVal)
        }
        return ""
    }

    fun todayDate(date: String?): String? {
        val readFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val writeFormat: DateFormat = SimpleDateFormat("hh:mm aa")
        var dateVal: Date? = null
        try {
            dateVal = readFormat.parse(date)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (dateVal != null) {
            return writeFormat.format(dateVal)
        }
        return ""
    }

    fun chatBold(msg: String?): String {
        val dataMsg = "<b>" + msg + "</b>"
        val msgVal = Html.fromHtml(dataMsg)
        return msgVal.toString()
    }

    fun getRandomString(n: Int): String? {
        val alphaNumericString = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz")
        val sb = java.lang.StringBuilder(n)
        for (i in 0 until n) {
            val index = (alphaNumericString.length * Math.random()).toInt()
            sb.append(alphaNumericString[index])
        }
        return sb.toString()
    }

    fun isImageFile(path: String): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("image")
    }

    fun isPdfFile(path: String): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("application/pdf")
    }

    fun localTimeConvert(date: String): String? {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        df.timeZone = TimeZone.getTimeZone("GMT") // missing line

        val date: Date = df.parse(date)
        val tz = TimeZone.getDefault()
        val writeDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        writeDate.timeZone = TimeZone.getTimeZone(tz.id)
        val s = writeDate.format(date)
        Log.d("TAG", "localTimeConvert: " + s)
        return s
    }

    @SuppressLint("NewApi")
    fun localTimeConvertMail(date: String): String? {

        val odt: OffsetDateTime = OffsetDateTime.parse(date)
        val odtTruncatedToWholeSecond = odt.truncatedTo(ChronoUnit.SECONDS)
        val output = odtTruncatedToWholeSecond.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .replace("T", " ")

        return output
    }

    fun dateLastSeen(date: String): String {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        val one = Calendar.getInstance()
        one.add(Calendar.DATE, -2)
        val two = Calendar.getInstance()
        two.add(Calendar.DATE, -3)
        val three = Calendar.getInstance()
        three.add(Calendar.DATE, -4)
        val four = Calendar.getInstance()
        four.add(Calendar.DATE, -5)
        val five = Calendar.getInstance()
        five.add(Calendar.DATE, -6)

        return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
            "Last seen today @ " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
            "Last seen yesterday @ " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == one[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == one[Calendar.DAY_OF_YEAR]) {
            "Last seen " + dayName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == two[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == two[Calendar.DAY_OF_YEAR]) {
            "Last seen " + dayName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == three[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == three[Calendar.DAY_OF_YEAR]) {
            "Last seen " + dayName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == four[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == four[Calendar.DAY_OF_YEAR]) {
            "Last seen " + dayName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(date))
        } else if (calendar[Calendar.YEAR] == five[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == five[Calendar.DAY_OF_YEAR]) {
            "Last seen " + dayName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(date))
        } else {
            "Last seen " + monthName(localTimeConvert(date)!!) + " " + todayDate(localTimeConvert(
                date))
        }
    }

    fun dateMail(date: String): String {
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        val one = Calendar.getInstance()
        one.add(Calendar.DATE, -2)
        val two = Calendar.getInstance()
        two.add(Calendar.DATE, -3)
        val three = Calendar.getInstance()
        three.add(Calendar.DATE, -4)
        val four = Calendar.getInstance()
        four.add(Calendar.DATE, -5)
        val five = Calendar.getInstance()
        five.add(Calendar.DATE, -6)

        return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
            "Today - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
            "Yesterday - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == one[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == one[Calendar.DAY_OF_YEAR]) {
            dayNameMail(date) + " - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == two[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == two[Calendar.DAY_OF_YEAR]) {
            dayNameMail(date) + " - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == three[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == three[Calendar.DAY_OF_YEAR]) {
            dayNameMail(date) + " - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == four[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == four[Calendar.DAY_OF_YEAR]) {
            dayNameMail(date) + " - " + dateConvert(date)
        } else if (calendar[Calendar.YEAR] == five[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == five[Calendar.DAY_OF_YEAR]) {
            dayNameMail(date) + " - " + dateConvert(date)
        } else {
            dateConvert(date)
        }
    }

    fun dayNameMail(dates: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(dates)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        val formatter = SimpleDateFormat("EEEE")
        val newFormat = formatter.format(date)
        return newFormat
    }

    fun replyDayMail(dates: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(dates)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        val formatter = SimpleDateFormat("EEE")
        val newFormat = formatter.format(date)
        return newFormat
    }

    fun dayName(dates: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(dates)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        val formatter = SimpleDateFormat("EEEE")
        val newFormat = formatter.format(date)
        return newFormat
    }

    fun monthName(dates: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = sdf.parse(dates)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        val formatter = SimpleDateFormat("dd-MMM-yyyy")
        val newFormat = formatter.format(date)
        return newFormat
    }


    fun downloadFile(context: Context, url: String, value: String, mailCallback: MailCallback) {
        val file =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "com.smartstation" + "/Smart Station/Media/Smart Station Download/$value/")
        if (!file.exists()) {
            file.mkdirs()
        }


        val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri: Uri = Uri.parse(url)
        val file1 =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/com.smartstation" + "/Smart Station/Media/Smart Station Download/$value/" + downloadUri.lastPathSegment)

        if (file1.isFile) {
            return mailCallback.success(file1.absolutePath, true)
        }

        val request = DownloadManager.Request(
            downloadUri
        )
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI
                    or DownloadManager.Request.NETWORK_MOBILE
        )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverRoaming(false)
            .setDestinationUri(Uri.fromFile(file1))
        val s = mgr.enqueue(request)

        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("Range")
            override fun onReceive(context: Context, intent: Intent) {
                var intent = intent
                val action = intent.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0) != s) {
                        // Quick exit - its not our download!
                        return
                    }
                    val query = DownloadManager.Query()
                    query.setFilterById(s)
                    val c: Cursor = dm.query(query)
                    if (c.moveToFirst()) {
                        val columnIndex: Int = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (DownloadManager.STATUS_SUCCESSFUL == c
                                .getInt(columnIndex)
                        ) {
                            return mailCallback.success(file1.absolutePath, true)
                        } else {
                            return mailCallback.success("", false)
                        }
                    } else {
                        return mailCallback.success("", false)
                    }
                    context.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(
            DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }

    fun milliSecondsToTimer(milliseconds: Long): String? {
        return String.format(Locale.US, "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        )
    }

    fun getDuration(file: String): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return formateMilliSeccond(durationStr!!.toLong())
    }

    fun formateMilliSeccond(milliseconds: Long): String? {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        //      return  String.format("%02d Min, %02d Sec",
        //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
        //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
        //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        // return timer string
        return finalTimerString
    }

    fun getAudioPathAndDuration(context: Context, uri: Uri?): Array<String?>? {
        val cursor = context.contentResolver.query(uri!!,
            arrayOf(MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.DURATION),
            null,
            null,
            null)
        val audioArray = arrayOfNulls<String>(2)
        try {
            cursor!!.moveToFirst()
            val path = cursor.getString(0)
            val durationInMs = cursor.getInt(1)
            val duration: String = milliSecondsToTimer(durationInMs.toLong())!!
            audioArray[0] = path
            audioArray[1] = duration
        } catch (e: java.lang.Exception) {
        } finally {
            cursor!!.close()
        }
        return audioArray
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    fun loadImageFromURL(url: String?, name: String?): Drawable? {
        return try {
            val iss = URL(url).content as InputStream
            Drawable.createFromStream(iss, name)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    //this will highlight the text when user searches for message in chat
    fun highlightText(fullText: String): Spanned? {
        val wordtoSpan: Spannable = SpannableString(fullText)
        wordtoSpan.setSpan(BackgroundColorSpan(Color.YELLOW),
            0,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return wordtoSpan
    }

}