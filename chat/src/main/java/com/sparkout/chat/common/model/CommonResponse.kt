package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.ui.chat.view.ChatActivity
import com.sparkout.chat.ui.chat.view.adapter.ChatAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Created by krish on 02-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
data class CommonResponse(var status: Boolean, var message: String, var data: BodyData)
/* var status: Boolean = false
 var message: String = ""
 lateinit var body: BodyData*/

data class BodyData(
    var url: String,
    var uri: String,
    @SerializedName("media_type")
    var mediaType: String,
    @SerializedName("image_name")
    var imageName: String
)

/*
var url: String = ""
var uri: String = ""

@SerializedName("media_type")
var mediaType: String = ""

@SerializedName("image_name")
var imageName: String = ""
*/




