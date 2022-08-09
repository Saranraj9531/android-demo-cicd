package com.sparkout.chat.roomdb

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

// Created by krish on 10-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ConverterModel {
    @TypeConverter
    fun fromString(value: String?): ArrayList<String?>? {
        val listType =
            object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson<ArrayList<String?>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}