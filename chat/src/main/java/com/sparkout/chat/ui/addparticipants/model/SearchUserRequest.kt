package com.sparkout.chat.ui.addparticipants.model

import com.google.gson.annotations.SerializedName

class SearchUserRequest {
    @SerializedName("sender_id")
    var senderId: String = ""
    var indexValue: Int = 0
    var keyword: String = ""
}