package com.sparkout.chat.ui.chat.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import java.io.Serializable

// Created by krish on 20-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Entity(tableName = "Chat")
class ChatModel() : Parcelable {


    var sender: String = ""
    var receiver: String = ""
    var receivers: ArrayList<String> = ArrayList()
    var message: String = ""
    var userId: String = SharedPreferenceEditor.getData(USER_ID)

    @SerializedName("chat_type")
    var chatType: String = ""

    @SerializedName("chat_time")
    var chatTime: String = ""

    @SerializedName("message_status")
    var messageStatus: Int = 0

    @PrimaryKey
    @SerializedName("message_id")
    var messageId: String = ""

    @SerializedName("message_type")
    var messageType: String = ""

    @SerializedName("check_edit")
    var checkEdit: Boolean = false

    @SerializedName("is_playing")
    var isPlaying: Boolean = false

    @SerializedName("is_paused")
    var isPaused: Boolean = false

    @SerializedName("check_reply")
    var checkReply: Boolean = false

    @SerializedName("check_forwarded")
    var checkForwarded: Boolean = false

    @SerializedName("reply_message_id")
    var replyMessageId: String? = null
    var uri: String? = null

    @SerializedName("lat")
    var latitude: Double? = null

    @SerializedName("lng")
    var longitude: Double? = null
    var duration: Int? = null

    @SerializedName("check_blocked_by_user")
    var checkBlockedByUser: Boolean = false

    constructor(parcel: Parcel) : this() {
        sender = parcel.readString().toString()
        receiver = parcel.readString().toString()
        message = parcel.readString().toString()
        userId = parcel.readString().toString()
        chatType = parcel.readString().toString()
        chatTime = parcel.readString().toString()
        messageStatus = parcel.readInt()
        messageId = parcel.readString().toString()
        messageType = parcel.readString().toString()
        checkEdit = parcel.readByte() != 0.toByte()
        isPlaying = parcel.readByte() != 0.toByte()
        isPaused = parcel.readByte() != 0.toByte()
        checkReply = parcel.readByte() != 0.toByte()
        checkForwarded = parcel.readByte() != 0.toByte()
        replyMessageId = parcel.readString()
        uri = parcel.readString()
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        duration = parcel.readValue(Int::class.java.classLoader) as? Int
        checkBlockedByUser = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sender)
        parcel.writeString(receiver)
        parcel.writeString(message)
        parcel.writeString(userId)
        parcel.writeString(chatType)
        parcel.writeString(chatTime)
        parcel.writeInt(messageStatus)
        parcel.writeString(messageId)
        parcel.writeString(messageType)
        parcel.writeByte(if (checkEdit) 1 else 0)
        parcel.writeByte(if (isPlaying) 1 else 0)
        parcel.writeByte(if (isPaused) 1 else 0)
        parcel.writeByte(if (checkReply) 1 else 0)
        parcel.writeByte(if (checkForwarded) 1 else 0)
        parcel.writeString(replyMessageId)
        parcel.writeString(uri)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(duration)
        parcel.writeByte(if (checkBlockedByUser) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatModel> {
        override fun createFromParcel(parcel: Parcel): ChatModel {
            return ChatModel(parcel)
        }

        override fun newArray(size: Int): Array<ChatModel?> {
            return arrayOfNulls(size)
        }
    }


}