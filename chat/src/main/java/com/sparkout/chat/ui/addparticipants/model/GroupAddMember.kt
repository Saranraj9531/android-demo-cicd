package com.sparkout.chat.ui.addparticipants.model

import com.google.gson.annotations.SerializedName
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.groupchat.model.MembersModel

class GroupAddMember {
    @SerializedName("existing_members")
    lateinit var existingMembers: ArrayList<MembersModel>

    @SerializedName("new_members")
    lateinit var newMembers: ArrayList<MembersModel>

    @SerializedName("group_id")
    var groupId: String = ""
    var sender: String = ""
    lateinit var message: ChatModel
}