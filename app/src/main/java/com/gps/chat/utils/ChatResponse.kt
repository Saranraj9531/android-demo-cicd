package com.gps.chat.utils

import android.content.Context
import com.gps.chat.interfaces.ChatConversation
import com.gps.chat.utils.CONSTANTS.USER_ID
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.ui.chat.model.ChatListModel

class ChatResponse {
    companion object {
        private var mChatType: String = ""
        private var mContext: Context? = null
        private var chatPojoList: List<ChatListModel> = ArrayList()
        private var chatConversation: ChatConversation? = null

        fun conversation(context: Context, conversation: ChatConversation) {
            this.mContext = context
            this.chatConversation = conversation

            chatPojoList = mAppDatabase?.getChatListDao()!!
                .getChatList(SharedPreferencesEditor.getData(USER_ID))
            chatConversation?.conversationCallBack(chatPojoList)
        }
    }
}