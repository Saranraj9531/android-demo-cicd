package com.gps.chat.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.gps.chat.R
import com.gps.chat.ui.login.view.LoginActivity

private lateinit var mProgressDialog: Dialog


fun showToast(activity: Activity, mToastMessage: String) {
    Toast.makeText(activity, mToastMessage, Toast.LENGTH_SHORT).show()
}

fun showToast(activity: Context, mToastMessage: String) {
    Toast.makeText(activity, mToastMessage, Toast.LENGTH_SHORT).show()
}

fun String.isEmptyCheck(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    return true
}

fun String.passwordValidation(): Boolean {
    if (this.length in 6..15) {
        return true
    }
    return false
}

fun passwordCompareValidation(oldPassword: String, newPassword: String): Boolean {
    if (oldPassword == newPassword) {
        return true
    }
    return false
}

fun hideKeyboard(context: Context, view: View) {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun preventDoubleClick(view: View) {
    view.isEnabled = false
    view.postDelayed({ view.isEnabled = true }, 500)
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

/**
 * function to show the loading
 *
 * @param context - to access the context
 */
fun showProgressDialog(context: Context?) {
    if (context != null) {
        mProgressDialog = Dialog(context)
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mProgressDialog.setCancelable(false)
        mProgressDialog.setContentView(R.layout.layout_progress)

        mProgressDialog.show()
    }
}

/**
 * function to remove the loading
 */
fun removeProgressDialog() {
    try {
        if (::mProgressDialog.isInitialized) {
            mProgressDialog.dismiss()
        } else {
            mProgressDialog.dismiss()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


//TODO: fun to show snackbar with action
fun snackBarAction(
    context: Context,
    view: View?,
    message: String?
) {
    val snackBar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
    val sbView = snackBar.view
    sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))
    val textView =
        sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackBar.setAction(context.resources.getString(R.string.str_settings)) {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(
                    "package",
                    context.packageName,
                    null
                )
            )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.white))
    snackBar.show()
}


fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.show(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun glideRequestOptionProfile(): RequestOptions {
    return RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(true)
        .centerCrop()
        .dontAnimate()
        .placeholder(R.drawable.ic_default_user)
        .error(R.drawable.ic_default_user)
        .priority(Priority.IMMEDIATE)
        .encodeFormat(Bitmap.CompressFormat.PNG)
        .format(DecodeFormat.DEFAULT)
}

/**
 * function to logout authentication fail
 */
fun errorAuthenticationLogout(context: Activity) {
    SharedPreferencesEditor.clearAllData()
    context.startActivity(Intent(context, LoginActivity::class.java))
    context.finishAffinity()
}



