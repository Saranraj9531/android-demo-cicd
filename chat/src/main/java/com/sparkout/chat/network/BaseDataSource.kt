package com.sparkout.chat.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

abstract class BaseDataSource {

    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Resource<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) return Resource.success(body)
            }
            return errorHandling(response)
        } catch (e: Exception) {
            Log.e("Nive ", "getResult:Error ${e.toString()} ")
            return error(e.message ?: e.toString(), 0)
        }
    }

    private fun <T> error(message: String, responseCode: Int): Resource<T> {
        return Resource.error(message, responseCode)
    }

    private fun <T> errorHandling(response: Response<T>): Resource<T> {
        val gson = Gson()
        val type = object : TypeToken<T>() {}.type
        val errorResponse: T? = gson.fromJson(response.errorBody()?.string(), type)
        kotlin.runCatching {
            val errorGenericType = object : TypeToken<ErrorGenericResponse<T>>() {}.type
            val errorGenericResponse: ErrorGenericResponse<T>? =
                gson.fromJson(Gson().toJson(errorResponse), errorGenericType)
            val map: java.util.HashMap<*, *>? =
                Gson().fromJson(Gson().toJson(errorGenericResponse?.message), HashMap::class.java)
            var message = Gson().toJson(errorGenericResponse?.message)
            val responseCode = errorGenericResponse?.statusCode?.toInt() ?: 0
            val sb = StringBuilder()
            map?.mapKeys {
                sb.append(it.value.toString().plus("\n\n"))
            }
            message = sb.toString()
            return error(message, responseCode)
        }.runCatching {
            kotlin.runCatching {
                val errorBodyType = object : TypeToken<ErrorBody>() {}.type
                val errorBodyResponse: ErrorBody? =
                    gson.fromJson(Gson().toJson(errorResponse), errorBodyType)
                val message = errorBodyResponse?.message.toString()
                val responseCode = errorBodyResponse?.statusCode ?: 0
                return error(message, responseCode)
            }
        }
        return error(" ${response.code()} - ${response.message()}", response.code())
    }

    data class ErrorBody(val status: Boolean, val message: String, val statusCode: Int)
    data class ErrorGenericResponse<T>(val status: Boolean, val message: T, val statusCode: Double)
}