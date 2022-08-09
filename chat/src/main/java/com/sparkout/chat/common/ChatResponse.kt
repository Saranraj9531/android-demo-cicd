package com.sparkout.chat.common

import android.content.Context
import com.sparkout.chat.clickinterface.ChatConversation
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.ui.chat.model.ChatListModel

// Created by krish on 31-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ChatResponse {
    companion object {
        private var mChatType: String = ""
        private var mContext: Context? = null
        private var chatPojoList: List<ChatListModel>? = null
        private var chatConversation: ChatConversation? = null

        fun conversation(context: Context, conversation: ChatConversation) {
            this.mContext = context
            this.chatConversation = conversation

            chatPojoList = mAppDatabase!!.getChatListDao().getChatList(SharedPreferenceEditor.getData(USER_ID))
            chatConversation!!.conversationCallBack(chatPojoList!!)
        }
    }
}