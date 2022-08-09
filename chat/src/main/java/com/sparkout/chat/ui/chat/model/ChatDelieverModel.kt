package com.sparkout.chat.ui.chat.model

import com.google.gson.annotations.SerializedName

// Created by krish on 05-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ChatDelieverModel {
    var sender: String = ""
    var receiver: String = ""

    @SerializedName("message_id")
    var messageId: String = ""
}