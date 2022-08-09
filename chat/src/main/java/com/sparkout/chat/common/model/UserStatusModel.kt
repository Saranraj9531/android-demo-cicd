package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName

// Created by krish on 17-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class UserStatusModel {
    @SerializedName("_id")
    var id: String = ""

    @SerializedName("last_seen")
    var lastSeen: String = ""
}