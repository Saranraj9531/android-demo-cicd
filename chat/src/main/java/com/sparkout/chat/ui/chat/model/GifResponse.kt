package com.sparkout.chat.ui.chat.model

import java.util.*
import kotlin.collections.ArrayList

// Created by krish on 17-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class GifResponse {
    var data: ArrayList<GifData> = ArrayList()

    class GifData {
        lateinit var images: Images
    }

    class Images {
        lateinit var original: Original
    }

    class Original {
        lateinit var url: String
    }
}