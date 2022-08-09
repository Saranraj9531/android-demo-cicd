package com.gps.chat.interfaces

import com.sparkout.chat.ui.chat.model.ChatListModel


interface ChatConversation {
    fun conversationCallBack(listConversation: List<ChatListModel>)

}