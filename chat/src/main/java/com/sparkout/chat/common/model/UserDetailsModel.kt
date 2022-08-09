package com.sparkout.chat.common.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.sparkout.chat.roomdb.ConverterModel
@Entity(tableName = "User")
class UserDetailsModel() : Parcelable {


    @SerializedName("user_status")
    var userStatus: Int = 0

    @SerializedName("user_type")
    var userType: Int = 0

    @SerializedName("acknowledged_count")
    var acknowledgedCount: Int = 0

    @TypeConverters(ConverterModel::class)
    @SerializedName("acknowledged_users")
    lateinit var acknowledgedUsers: ArrayList<String>

    @TypeConverters(ConverterModel::class)
    lateinit var contacts: ArrayList<String>

    @SerializedName("_id")
    @PrimaryKey
    var id: String = ""
    var name: String? = null
    var username: String? = null
    var gender: String? = null

    @SerializedName("date_of_birth")
    var dateOfBirth: String? = null

    @SerializedName("profile_picture")
    var profilePicture: String? = null

    @SerializedName("passport_number")
    var passportNumber: String? = null

    @SerializedName("passport_image")
    var passportImage: String? = null

    @SerializedName("last_seen")
    var lastSeen: String? = null
    var checkOffline: Boolean? = null

    constructor(parcel: Parcel) : this() {
        userStatus = parcel.readInt()
        userType = parcel.readInt()
        acknowledgedCount = parcel.readInt()
        id = parcel.readString().toString()
        name = parcel.readString()
        username = parcel.readString()
        gender = parcel.readString()
        dateOfBirth = parcel.readString()
        profilePicture = parcel.readString()
        passportNumber = parcel.readString()
        passportImage = parcel.readString()
        lastSeen = parcel.readString()
        checkOffline = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userStatus)
        parcel.writeInt(userType)
        parcel.writeInt(acknowledgedCount)
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(username)
        parcel.writeString(gender)
        parcel.writeString(dateOfBirth)
        parcel.writeString(profilePicture)
        parcel.writeString(passportNumber)
        parcel.writeString(passportImage)
        parcel.writeString(lastSeen)
        parcel.writeValue(checkOffline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserDetailsModel> {
        override fun createFromParcel(parcel: Parcel): UserDetailsModel {
            return UserDetailsModel(parcel)
        }

        override fun newArray(size: Int): Array<UserDetailsModel?> {
            return arrayOfNulls(size)
        }
    }
}