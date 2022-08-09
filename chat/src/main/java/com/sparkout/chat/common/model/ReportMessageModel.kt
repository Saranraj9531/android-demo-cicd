package com.sparkout.chat.common.model

import com.google.gson.annotations.SerializedName

class ReportMessageModel {
    var user: String = ""

    @SerializedName("message_id")
    var messageId: String = ""

    @SerializedName("reported_by")
    var reportedBy: String = ""
}