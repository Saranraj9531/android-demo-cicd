package com.sparkout.chat.common

import android.util.Log
import timber.log.Timber

// Created by krish on 20-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ReleaseTree:Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }
    }
}