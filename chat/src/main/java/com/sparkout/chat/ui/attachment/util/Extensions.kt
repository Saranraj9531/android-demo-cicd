package com.sparkout.chat.ui.attachment.util

import android.content.res.Resources

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()