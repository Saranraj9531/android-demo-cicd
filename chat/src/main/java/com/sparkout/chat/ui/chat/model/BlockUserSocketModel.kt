package com.sparkout.chat.ui.chat.model

import com.google.gson.annotations.SerializedName

/**
 *Created by Krishnaprakash K on 03-04-2021.
 *Copyright (c) 2021 Sparkout Tech Solutions LLP. All rights reserved.
 */
class BlockUserSocketModel {
    var sender: String = ""

    @SerializedName("block_user")
    var blockUser: String? = null

    @SerializedName("unblock_user")
    var unblockUser: String? = null
}