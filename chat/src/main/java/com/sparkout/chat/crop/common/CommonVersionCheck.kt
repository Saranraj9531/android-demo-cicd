package com.sparkout.chat.crop.common

import android.os.Build
import android.os.Build.VERSION.SDK_INT

object CommonVersionCheck {

    fun isAtLeastM23() = SDK_INT >= Build.VERSION_CODES.M
    fun isAtLeastQ29() = SDK_INT >= Build.VERSION_CODES.Q
}
