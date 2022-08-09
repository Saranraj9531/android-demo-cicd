package com.gps.chat.ui.home.model

import com.google.gson.annotations.SerializedName

class AdResponse {
    var status: Boolean = false
    var message: String = ""
    var data: ArrayList<AdDetails> = ArrayList()

    inner class AdDetails {

        @SerializedName("ad_image")
        var adImage: String? = null
    }

}


