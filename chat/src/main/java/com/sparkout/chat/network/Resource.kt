package com.sparkout.chat.network

/**
 *Created by Nivetha S on 11-11-2021.
 */
data class Resource<out T>(val status: Status, val data: T?, val message: String?, val responseCode: Int) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null, 0)
        }

        fun <T> error(message: String, responseCode: Int, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, message, responseCode)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null, 0)
        }
    }
}