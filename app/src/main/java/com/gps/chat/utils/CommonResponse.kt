package com.gps.chat.utils

import com.google.gson.annotations.SerializedName

class CommonResponse {

    var status: Boolean = false
    var message: String = ""
    var data: BodyData? = null

    class BodyData {
        var url: String = ""
        var uri: String = ""

        @SerializedName("media_type")
        var mediaType: String = ""
    }
}