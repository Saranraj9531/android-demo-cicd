package com.sparkout.chat.common.chatenum

// Created by krish on 31-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
enum class ChatMessageTypes(val type: String) {
    TEXT("1"),
    AUDIO("2"),
    VIDEO("3"),
    IMAGE("4"),
    LOCATION("5"),
    LIVELOCATION("6"),
    DOCUMENT("7"),
    GIF("8"),
    CREATEGROUP("9"),
    GROUPINFO("10"),
    GROUPPROFILE("11"),
    ADDMEMBER("12"),
    REMOVEMEMBER("13"),
    EXITMEMBER("14"),
    ADDADMIN("15"),
    REMOVEADMIN("16"),
    DELETEFOREVERYONE("17"),
    DATE("101"),
    UNREAD("102");
}