package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName

/**
 *Created by Krishnaprakash K on 20-04-2021.
 *Copyright (c) 2021 Sparkout Tech Solutions LLP. All rights reserved.
 */
class GetBlockedContactsModel {
    lateinit var sender: ArrayList<String>
    lateinit var receivers: ArrayList<ReceiversIdModel>

    inner class ReceiversIdModel {
        @SerializedName("_id")
        var id: String = ""
    }
}