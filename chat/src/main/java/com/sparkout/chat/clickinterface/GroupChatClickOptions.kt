package com.sparkout.chat.clickinterface

import android.net.Uri
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.chat.view.adapter.ChatAdapter
import com.sparkout.chat.ui.groupchat.view.adapter.GroupChatAdapter

// Created by krish on 27-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
interface GroupChatClickOptions {
    fun audioOptions(audioholder: GroupChatAdapter.AudioViewHolder, chatModel: ChatModel, uri: Uri)

    fun videoOptions(chatModel: ChatModel)

    fun imageOptions(chatModel: ChatModel)

    fun documentOptions(chatModel: ChatModel)

    fun locationOptions(chatModel: ChatModel)

    fun retryOptions(chatModel: ChatModel)

    fun replyRedirection(mReplyMessageId: String)
}