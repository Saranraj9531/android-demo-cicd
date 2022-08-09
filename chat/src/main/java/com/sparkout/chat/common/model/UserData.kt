package com.sparkout.chat.common.model

// Created by krish on 06-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class UserData {
    var id: String = ""
    var tUid: String = ""
    var mobileNo: String = ""
    var username: String = ""
    var profilePicture: String = ""
    var token: String = ""
    var gender: String = ""
    var dateOfBirth: String = ""
    lateinit var session:SessionData

    inner class SessionData{
        var authorization:String=""
    }
}