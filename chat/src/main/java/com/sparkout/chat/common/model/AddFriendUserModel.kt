package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName

class AddFriendUserModel {
    lateinit var data: ArrayList<DataModel>

    inner class DataModel {
        @SerializedName("_id")
        var id: String = ""
    }
}