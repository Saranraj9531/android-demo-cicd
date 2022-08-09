package com.sparkout.chat.clickinterface

import com.sparkout.chat.ui.chat.model.GifResponse

// Created by krish on 27-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
interface GifClickOptions {
    fun gifOptions(gifData: GifResponse.GifData)
}