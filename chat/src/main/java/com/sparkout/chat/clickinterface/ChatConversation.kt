package com.sparkout.chat.clickinterface

import com.sparkout.chat.ui.chat.model.ChatListModel

// Created by krish on 31-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
interface ChatConversation {
    fun conversationCallBack(listConversation: List<ChatListModel>)
}