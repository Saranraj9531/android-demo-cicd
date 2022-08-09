package com.sparkout.chat.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.database.CursorWindow
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.style.TypefaceSpan
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.util.IOUtils
import com.google.android.material.snackbar.Snackbar
import com.sparkout.chat.R
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import java.io.*
import java.lang.reflect.Field
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class BaseUtils {
    companion object {
        private lateinit var mProgressDialog: Dialog
        const val GIF_URL: String = "http://api.giphy.com/v1/gifs/"
        const val GIPHY_API_KEY: String = "FvGC0dw5SVfgPlemmWoWrADegGqZTRgU"

        //directory paths
        val sPhotoDirectoryPath = Environment.getExternalStorageDirectory()
                                      .toString() + "/Bee Bush Messenger/Photos"
        val sVideoDirectoryPath = Environment.getExternalStorageDirectory()
                                      .toString() + "/Bee Bush Messenger/Videos"
        val sDocumentDirectoryPath = Environment.getExternalStorageDirectory()
                                         .toString() + "/Bee Bush Messenger/Documents"
        val sSendAudioDirectoryPath = Environment.getExternalStorageDirectory()
                                          .toString() + "/Bee Bush Messenger/Voice"
        const val HEADER_AUTHORIZATION_KEY_FOR_TOKEN = "AUTHORIZATIONKEYFORTOKEN"
        const val HEADER_LOGIN_STATUS = "LOGINSTATUS"
        const val HEADER_USER_ID = "USERTUID"
        const val HEADER_DEVICE_ID = "DEVICEID"
        const val HEADER_VERSION = "VERSION"
        const val HEADER_DEVICE_TYPE = "DEVICETYPE"
        const val HEADER_KEY = "key"
        const val HEADER_CHAT_KEY = "chat_key"
        val SDF =
            SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault())
        const val CHAT_KEY =
            "E10ajlspi0980s8af08jsajKLJKUJOHY0808dfgesjd43095jojdfkljlHJNjbhgghjjkkkJjjhhfEYIOgGFdcvnbKLP878gjghjghjhgjg09090k098797ldjlhgfgdgFhdfkg"
        const val KEY =
            "E09F1280ghjghjg606C3BF43D882F479032F03B2C4172B795F997E03FA356604CA06A2C7090DBD6380454C39FD57BFCC6A24C712795021FB9501DBA54719285AFBC5AE2"

        //constants
        const val AUDIO_REQUEST_CODE: Int = 1
        const val CAMERA_REQUEST_CODE: Int = 2
        const val STORAGE_REQUEST_CODE: Int = 3
        const val MULTIPLE_IMAGE_SELECT: Int = 4
        const val MY_REQUEST_CODE_VIDEO: Int = 5
        const val MY_REQUEST_CODE_DOCUMENT: Int = 6
        const val DOCUMENT_PICKER_SELECT: Int = 7
        const val PLACE_PICKER_REQUEST: Int = 8
        const val PHOTO_EDIT_CODE: Int = 9
        const val MEDIA_REQUEST_CODE: Int = 9
        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun snackBar(activity: Context, view: View, mMessage: String) {
            val mRegularTypeface =
                Typeface.createFromAsset(activity.assets, "roboto_regular.ttf")
            val snackBar: Snackbar = Snackbar.make(view, mMessage, Snackbar.LENGTH_SHORT)
            val snackBarView = snackBar.view
            snackBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_primary))
            val textView: AppCompatTextView =
                snackBarView.findViewById(com.google.android.material.R.id.snackbar_text)
            textView.setTextColor(ContextCompat.getColor(activity, R.color.white))
            textView.typeface = mRegularTypeface
            snackBar.show()
        }

        //TODO: fun to hide keyboard
        fun hideKeyboard(context: Context, view: View) {
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        //TODO: prevent double clicking views
        fun preventDoubleClick(view: View) {
            view.isEnabled = false
            view.postDelayed({ view.isEnabled = true }, 500)
        }

        fun getProgressPercentage(currentDuration: Long,
                                  totalDuration: Long): Int {
            var percentage = 0.toDouble()
            val currentSeconds: Long = (currentDuration / 1000)
            val totalSeconds: Long = (totalDuration / 1000)
            // calculating percentage
            percentage = currentSeconds.toDouble() / totalSeconds * 100
            // return percentage
            return percentage.toInt()
        }

        /**
         * function to show the loading
         *
         * @param context - to access the context
         */
        fun showProgressDialog(context: Activity?) {
            if (context != null) {
                mProgressDialog = Dialog(context)
                mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                mProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                mProgressDialog.setCancelable(false)
                mProgressDialog.setContentView(R.layout.layout_progress)
                if (mProgressDialog != null && !mProgressDialog.isShowing && !context.isDestroyed) {
                    mProgressDialog.show()
                }
            }
        }

        /**
         * function to remove the loading
         */
        fun removeProgressDialog() {
            try {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss()
                } else {
                    mProgressDialog.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //TODO: fun to validate email pattern
        fun emailPattern(strEmailAddress: String): Boolean {
            if (TextUtils.isEmpty(strEmailAddress) || strEmailAddress.isEmpty()) {
                return false
            }
            val emailPattern = Pattern
                .compile(
                    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
                        )
            val emailMatcher = emailPattern.matcher(strEmailAddress)
            return emailMatcher.matches()
        }

        /**
         * function custom toast
         *
         * @param activity      - to access the context
         * @param mToastMessage - info to show in the view
         */
        fun showToast(activity: Activity, mToastMessage: String?) {
            Toast.makeText(activity, mToastMessage, Toast.LENGTH_SHORT).show()
        }

        /**
         * function custom toast
         *
         * @param context      - to access the context
         * @param mToastMessage - info to show in the view
         */
        fun showToast(context: Context, mToastMessage: String?) {
            Toast.makeText(context, mToastMessage, Toast.LENGTH_SHORT).show()
        }

        //TODO: fun to show snackbar with action
        fun snackBarAction(context: Context,
                           view: View?,
                           message: String?) {
            val snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
            val sbview = snackbar.view
            sbview.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))
            val textView =
                sbview.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            snackbar.setAction(context.resources.getString(R.string.str_settings)) {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                           Uri.fromParts("package",
                                         context.packageName,
                                         null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
            snackbar.show()
        }

        /**
         * custom font apply function
         */
        fun applyFontToMenuItem(mi: MenuItem, context: Context) {
            val font =
                Typeface.createFromAsset(context.assets, "roboto_regular.ttf")
            val mNewTitle = SpannableString(mi.title)
            mNewTitle.setSpan(CustomTypefaceSpan("", font),
                              0,
                              mNewTitle.length,
                              Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            mi.title = mNewTitle
        }

        fun imgRotate(filePath: String, bitmap: Bitmap): Bitmap {
            var rotatedBitmap: Bitmap? = null
            try {
                val ei = ExifInterface(filePath)
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                                                    )
                rotatedBitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90  -> TransformationUtils.rotateImage(
                        bitmap,
                        90
                                                                                           )
                    ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(
                        bitmap,
                        180
                                                                                           )
                    ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(
                        bitmap,
                        270
                                                                                           )
                    ExifInterface.ORIENTATION_NORMAL     -> bitmap
                    else                                 -> bitmap
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return rotatedBitmap!!
        }

        fun uriToBitmap(selectedFileUri: Uri?,
                        context: Context): Bitmap? {
            return try {
                val parcelFileDescriptor =
                    context.contentResolver.openFileDescriptor(selectedFileUri!!, "r")
                val fileDescriptor =
                    parcelFileDescriptor!!.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                image
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun glideRequestOptionProfile(): RequestOptions {
            return RequestOptions()
                .placeholder(R.drawable.default_user)
                .error(R.drawable.default_user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
        }

        class CustomTypefaceSpan(family: String?, private val newType: Typeface) :
            TypefaceSpan(family) {
            override fun updateDrawState(ds: TextPaint) {
                applyCustomTypeFace(ds, newType)
            }

            override fun updateMeasureState(paint: TextPaint) {
                applyCustomTypeFace(paint, newType)
            }

            companion object {
                private fun applyCustomTypeFace(paint: Paint,
                                                tf: Typeface) {
                    val oldStyle: Int
                    val old = paint.typeface
                    oldStyle = old?.style ?: 0
                    val fake = oldStyle and tf.style.inv()
                    if (fake and Typeface.BOLD != 0) {
                        paint.isFakeBoldText = true
                    }
                    if (fake and Typeface.ITALIC != 0) {
                        paint.textSkewX = -0.25f
                    }
                    paint.typeface = tf
                }
            }
        }

        fun showHideKeyboard(activity: Activity,
                             mHide: Boolean,
                             edittext_message: AppCompatEditText) {
            val view = activity.currentFocus
            if (null != view) {
                val mInputMethodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (mHide) {
                    mInputMethodManager.hideSoftInputFromWindow(edittext_message.applicationWindowToken,
                                                                0)
                } else {
                    edittext_message.requestFocus()
                    mInputMethodManager.showSoftInput(edittext_message, 0)
                }
            }
        }

        /*TODO: fun get the mime type of media type*/
        fun getMimeType(url: String?): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }

        fun getBitmapFromUri(resultUri: Uri?,
                             activity: Activity): Bitmap? {
            val bitmap: Bitmap
            return try {
                bitmap =
                    BitmapFactory.decodeStream(activity.contentResolver.openInputStream(resultUri!!))
                bitmap
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e("TAG", "getBitmapFromUri: " + e.printStackTrace())
                null
            }
        }

        fun getMessageId(mMessageId: String): String {
            return Base64.encodeToString(mMessageId.toByteArray(), Base64.NO_WRAP)
        }

        fun getGroupId(mGroupId: String): String {
            return Base64.encodeToString(mGroupId.toByteArray(), Base64.NO_WRAP)
        }

        fun getObjectId(mObjectId: String): String {
            return Base64.encodeToString(mObjectId.toByteArray(), Base64.NO_WRAP)
        }

        fun fileExist(mPath: String?): Boolean {
            val file = File(mPath!!)
            return file.exists()
        }

        fun loadAudioPath(mContext: Context, mMessageId: String?, i: Int): String? {
            try {
                val cw = ContextWrapper(mContext)
                var dir: File? = null
                if (i == 1) {
                    val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                    dir = File(folder.absolutePath)
                } else if (i == 2) {
                    val audioDirectoryPath =
                        Environment.getExternalStorageDirectory().toString().plus("/")
                            .plus(Environment.DIRECTORY_PICTURES).plus("/")
                            .plus("Bee Bush Messenger Voice")
                    dir = File(audioDirectoryPath)
                }
                if (!dir!!.exists()) {
                    dir.mkdir()
                }
                val listFile: Array<File> = dir.listFiles()!!
                if (listFile != null) {
                    for (file in listFile) {
                        if (file.name.contains(mMessageId!!)) {
                            return file.absolutePath
                        }
                    }
                }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun loadImagePath(mMessageId: String?): String? {
            try {
                val imageDirectoryPath =
                    Environment.getExternalStorageDirectory().toString().plus("/")
                        .plus(Environment.DIRECTORY_PICTURES).plus("/")
                        .plus("Bee Bush Messenger Photos")
                val dir = File(imageDirectoryPath)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val listFile: Array<File> = dir.listFiles()!!
                if (listFile != null) {
                    for (file in listFile) {
                        if (file.name.contains(mMessageId!!)) {
                            return file.absolutePath
                        }
                    }
                }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun loadVideoPath(mMessageId: String?): String? {
            try {
                val videoDirectoryPath =
                    Environment.getExternalStorageDirectory().toString().plus("/")
                        .plus(Environment.DIRECTORY_PICTURES).plus("/")
                        .plus("Bee Bush Messenger Videos")
                val dir = File(videoDirectoryPath)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val listFile: Array<File> = dir.listFiles()!!
                if (listFile != null) {
                    for (file in listFile) {
                        if (file.name.contains(mMessageId!!)) {
                            return file.absolutePath
                        }
                    }
                }
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        /*TODO: Verify if download is a success*/
        fun validDownload(context: Context,
                          downloadId: Long): Boolean {
            val manager =
                context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val c =
                manager.query(DownloadManager.Query().setFilterById(downloadId))
            try {
                if (c != null) {
                    if (c.moveToFirst()) {
                        val status =
                            c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        return status == DownloadManager.STATUS_SUCCESSFUL
                    }
                }
            } catch (e: Exception) {
                Log.e("Nive ", "validDownload: ${e.message}")
            } finally {
                c.close()
            }
            return false
        }

        /*TODO: fun to pick the video from storage*/
        fun pickImageFromStorage(mContext: Context,
                                 messageId: String?,
                                 path: Int): String? {
            val cw = ContextWrapper(mContext)
            var dir: File? = null
            if (path == 1) {
                val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
                dir = File(folder.absolutePath)
            } else if (path == 2) {
                val folder = cw.getDir("Videos", Context.MODE_PRIVATE)
                dir = File(folder.absolutePath)
            } else if (path == 3) {
                val folder = cw.getDir("Voice", Context.MODE_PRIVATE)
                dir = File(folder.absolutePath)
            } else {
                dir = File(sDocumentDirectoryPath)
            }
            val listFile = dir.listFiles()
            if (listFile != null) {
                for (file in listFile) {
                    if (file.name.contains(messageId!!)) {
                        return file.absolutePath
                    }
                }
            }
            return null
        }

        fun requestOptionsD(): RequestOptions? {
            return RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.default_user)
                .error(R.drawable.default_user)
                .priority(Priority.IMMEDIATE)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .format(DecodeFormat.DEFAULT)
        }

        fun requestOptionsT(): RequestOptions? {
            return RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_thumbnail_photo)
                .error(R.drawable.ic_thumbnail_photo)
                .priority(Priority.IMMEDIATE)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .format(DecodeFormat.DEFAULT)
        }

        fun requestOptionsTv(): RequestOptions? {
            return RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_thumbnail_video)
                .error(R.drawable.ic_thumbnail_video)
                .priority(Priority.IMMEDIATE)
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .format(DecodeFormat.DEFAULT)
        }

        fun infoMessageTypes(): ArrayList<String> {
            val mListInfoMessagetypes = ArrayList<String>()
            mListInfoMessagetypes.add(ChatMessageTypes.CREATEGROUP.type)
            mListInfoMessagetypes.add(ChatMessageTypes.GROUPINFO.type)
            mListInfoMessagetypes.add(ChatMessageTypes.GROUPPROFILE.type)
            mListInfoMessagetypes.add(ChatMessageTypes.ADDMEMBER.type)
            mListInfoMessagetypes.add(ChatMessageTypes.REMOVEMEMBER.type)
            mListInfoMessagetypes.add(ChatMessageTypes.EXITMEMBER.type)
            mListInfoMessagetypes.add(ChatMessageTypes.ADDADMIN.type)
            mListInfoMessagetypes.add(ChatMessageTypes.REMOVEADMIN.type)
            mListInfoMessagetypes.add(ChatMessageTypes.UNREAD.type)
            return mListInfoMessagetypes
        }

        // TODO allocate cursor size increased and catch exception especially in samsung device
        // TODO android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=363 (# cursors opened by this proc=363)
        fun fix() {
            try {
                val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
                field.isAccessible = true
                field.set(null, 102400 * 1024) //the 102400 is the new size added
                Log.e("Nive ", "fix: size added")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.e("Nive ", "fix:Cursor ${e.message} ")
            }
        }

        fun getInfoMessageTypes(mMessageType: String): Boolean {
            when (mMessageType) {
                ChatMessageTypes.CREATEGROUP.type  -> {
                    return true
                }
                ChatMessageTypes.GROUPINFO.type    -> {
                    return true
                }
                ChatMessageTypes.GROUPPROFILE.type -> {
                    return true
                }
                ChatMessageTypes.ADDMEMBER.type    -> {
                    return true
                }
                ChatMessageTypes.REMOVEMEMBER.type -> {
                    return true
                }
                ChatMessageTypes.EXITMEMBER.type   -> {
                    return true
                }
                ChatMessageTypes.ADDADMIN.type     -> {
                    return true
                }
                ChatMessageTypes.REMOVEADMIN.type  -> {
                    return true
                }
                ChatMessageTypes.UNREAD.type       -> {
                    return true
                }
                else                               -> {
                    return false
                }
            }
        }

        fun getLastMessage(mMessageType: String): String {
            when (mMessageType) {
                ChatMessageTypes.TEXT.type         -> {
                    return "Text"
                }
                ChatMessageTypes.AUDIO.type        -> {
                    return "Audio"
                }
                ChatMessageTypes.VIDEO.type        -> {
                    return "Video"
                }
                ChatMessageTypes.IMAGE.type        -> {
                    return "Image"
                }
                ChatMessageTypes.DOCUMENT.type     -> {
                    return "Document"
                }
                ChatMessageTypes.GIF.type          -> {
                    return "Gif"
                }
                ChatMessageTypes.LOCATION.type     -> {
                    return "Location"
                }
                ChatMessageTypes.LIVELOCATION.type -> {
                    return "Live Location"
                }
                else                               -> {
                    return ""
                }
            }
        }

        fun getUTCTime(): String {
            /*val mLong: Long = Instant.now().toEpochMilli()
            return mLong.toString()*/
            val mCalendar = Calendar.getInstance()
            val mUTCMilliseconds = mCalendar.timeInMillis
            return mUTCMilliseconds.toString()
        }

        fun getDate(milliSeconds: Long, dateFormat: String): String {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(dateFormat)
            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }

        @SuppressLint("SimpleDateFormat")
        fun getLocalTime(mTime: Long): String {
            val simpleDateFormatLocal = SimpleDateFormat("yyyy-MM-dd hh:mm a")
            simpleDateFormatLocal.timeZone = TimeZone.getDefault()
            return simpleDateFormatLocal.format(mTime)
        }

        fun getStaticMap(context: Context, mLatLng: String): String? {
            return ("http://maps.googleapis.com/maps/api/staticmap?center="
                    + mLatLng
                    + "&zoom=16"
                    + "&maptype=roadmap"
                    + "&size=300x200"
                    + "&sensor=false"
                    + "&path=color:0x0000FF80"
                    + "&key="
                    + context.resources.getString(R.string.str_map_key))
        }

        fun getSmsTodayYestFromMilli(msgTimeMillis: Long): String? {
            val messageTime = Calendar.getInstance()
            messageTime.timeInMillis = msgTimeMillis
            // get Currunt time
            val now = Calendar.getInstance()
            val strDateFormate = "dd MMMM yyyy"
            Log.e("Nive ", "getSmsTodayYestFromMilli: " + now[Calendar.DATE])
            Log.e("Nive ", "getSmsTodayYestFromMilli:msgTme " + messageTime[Calendar.DATE])
            return if (now[Calendar.DATE] == messageTime[Calendar.DATE] &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                "Today"
            } else if (now[Calendar.DATE] - messageTime[Calendar.DATE] == 1
                       &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                "Yesterday"
            } else {
                "" + DateFormat.format(strDateFormate, messageTime)
            }
        }

        fun getLastSeenFromMilli(msgTimeMillis: Long): String? {
            val messageTime = Calendar.getInstance()
            messageTime.timeInMillis = msgTimeMillis
            // get Currunt time
            val now = Calendar.getInstance()
            val timeFormatString = "hh:mm a"
            val strDateFormate = "dd MMMM yyyy"
            return if (now[Calendar.DATE] == messageTime[Calendar.DATE] &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                "today at".plus(" ").plus(DateFormat.format(timeFormatString, messageTime))
            } else if (now[Calendar.DATE] - messageTime[Calendar.DATE] == 1
                       &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                "yesterday at".plus(" ").plus(DateFormat.format(timeFormatString, messageTime))
            } else {
                "at " + DateFormat.format(strDateFormate, messageTime)
            }
        }

        fun getTime(mTime: String): String {
            val mLocalTime = mTime.split(" ").toTypedArray()
            val mMessageTime = mLocalTime[1]
            val mMessageMeridian = mLocalTime[2]
            return mMessageTime.plus(" ").plus(mMessageMeridian)
        }

        fun getChatDate(msgTimeMillis: Long): String? {
            val messageTime = Calendar.getInstance()
            messageTime.timeInMillis = msgTimeMillis
            // get Currunt time
            val now = Calendar.getInstance()
            val strDateFormate = "dd/MM/yy"
            return if (now[Calendar.DATE] == messageTime[Calendar.DATE] &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                val mTime = getLocalTime(msgTimeMillis)
                getTime(mTime)
            } else if (now[Calendar.DATE] - messageTime[Calendar.DATE] == 1
                       &&
                       now[Calendar.MONTH] == messageTime[Calendar.MONTH]
                       &&
                       now[Calendar.YEAR] == messageTime[Calendar.YEAR]) {
                "Yesterday"
            } else {
                "" + DateFormat.format(strDateFormate, messageTime)
            }
        }

        fun getMediaList(): ArrayList<String> {
            val mMediaList = ArrayList<String>()
            mMediaList.clear()
            mMediaList.add(ChatMessageTypes.AUDIO.type)
            mMediaList.add(ChatMessageTypes.VIDEO.type)
            mMediaList.add(ChatMessageTypes.IMAGE.type)
            return mMediaList
        }

        fun copy(context: Context,
                 srcUri: Uri?,
                 dstFile: File?) {
            try {
                val inputStream = context.contentResolver.openInputStream(srcUri!!) ?: return
                val outputStream: OutputStream = FileOutputStream(dstFile)
                IOUtils.copyStream(inputStream, outputStream)
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getFileName(uri: Uri?): String? {
            if (uri == null) return null
            var fileName: String? = null
            val path = uri.path
            val cut = path!!.lastIndexOf('/')
            if (cut != -1) {
                fileName = path.substring(cut + 1)
            }
            return fileName
        }

        /**
         * @param mDownloadUrl - download_url
         * @param mMessagetype - message_type
         * @param mMessageId   - messages_id
         */
        fun download(mContext: Context,
                     mDownloadUrl: String,
                     mMessagetype: String,
                     mMessageId: String,
                     mReceiverId: String) {
            if (!TextUtils.isEmpty(mDownloadUrl) && !mDownloadUrl.equals("null",
                                                                         ignoreCase = true)) {
                var mPath = ""
                var mTextName = ""
                if (mMessagetype == ChatMessageTypes.AUDIO.type) {
                    mTextName = "$mMessageId.wav"
                    mPath = "Bee Bush Messenger Voice/"
                } else if (mMessagetype == ChatMessageTypes.VIDEO.type) {
                    mTextName = "$mMessageId.mp4"
                    mPath = "Bee Bush Messenger Videos/"
                } else if (mMessagetype == ChatMessageTypes.IMAGE.type) {
                    mTextName = "$mMessageId.jpg"
                    mPath = "Bee Bush Messenger Photos/"
                } else if (mMessagetype == ChatMessageTypes.DOCUMENT.type) {
                    mTextName = "$mMessageId.pdf"
                    mPath = "Bee Bush Messenger/Documents"
                } else if (mMessagetype == ChatMessageTypes.GIF.type) {
                    mTextName = "$mMessageId.gif"
                    mPath = "Bee Bush Messenger/Gif"
                }
                /*mTextName =
                    "ADWFC78yhdAEYIOADWFC78yhdAEYIOADWFC78yhdAEYIOADWFC78yhdAEYIOADWFC78yhdAEYIOADWFC78yhdAEYIO789kiuFGTju456WEL0uhj346goilkp"*/
                Log.e("Nive ", "download:audio " + mMessageId)
                Log.e("Nive ", "download:audio Name" + mTextName.length)
                //                \u003d\u003d
                try {
                    val manager =
                        mContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    val request =
                        DownloadManager.Request(Uri.parse(mDownloadUrl))
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                                                              mPath.plus(mTextName))
                    manager.enqueue(request)
                } catch (e: java.lang.Exception) {
                    Log.e("TAG", "download: Exception " + e.message)
                }
            }
        }
    }
}