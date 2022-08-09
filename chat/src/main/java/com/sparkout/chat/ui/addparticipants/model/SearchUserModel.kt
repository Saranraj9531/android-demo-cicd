package com.sparkout.chat.ui.addparticipants.model

import com.google.gson.annotations.SerializedName
import com.sparkout.chat.common.model.UserDetailsModel

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class SearchUserModel {
    var status: Boolean = false
    var message: String = ""

    @SerializedName("total_count")
    var totalCount: Int = 0

    @SerializedName("data")
    lateinit var data: ArrayList<UserDetailsModel>
}