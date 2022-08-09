package com.sparkout.chat.ui.apisample

/**
 *Created by Nivetha S on 12-04-2021.
 */
sealed class Output<out T : Any>{
    data class Success<out T : Any>(val output : T) : Output<T>()
    data class Error(val exception: Exception)  : Output<Nothing>()
}