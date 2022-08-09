package com.sparkout.chat.ui.attachment.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatEditText

class MenuEditText : AppCompatEditText {
    var popupListener: PopupListener? = null

    interface PopupListener {
        fun getPopup(): PopupWindow
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
                                                                                  attrs,
                                                                                  defStyleAttr) {
    }

    /*@SuppressLint("NewApi")
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {

    }*/
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            popupListener?.let {
                val popup = it.getPopup()
                if (popup.isShowing) {
                    popup.dismiss()
                    return true
                }
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }
}