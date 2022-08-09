package com.sparkout.chat.ui.apisample

import android.util.Log
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import org.greenrobot.eventbus.EventBus
import retrofit2.Response
import java.io.IOException

/**
 *Created by Nivetha S on 12-04-2021.
 */
open class BaseRepository {
    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, error: String): T? {
        val result = newsApiOutput(call, error)
        var output: T? = null
        when (result) {
            is Output.Success -> {
                Log.e("Nive ", "safeApiCall:Sucess ")
                output = result.output
            }
            is Output.Error   -> {
                imageError(error, "Group")
                Log.e("Error", "The error and the ${result.exception}")
            }
        }
        return output
    }

    private suspend fun <T : Any> newsApiOutput(call: suspend () -> Response<T>,
                                                error: String): Output<T> {
        val response = call.invoke()
        return if (response.isSuccessful)
            Output.Success(response.body()!!)
        else
            Output.Error(IOException("OOps .. Something went wrong due to  $error"))
    }

    /*TODO: fun to set retry option*/
    private fun imageError(mMessageId: String, str: String) {
        try {
            val listChatModel = ChatApp.mAppDatabase!!.getChatDao().getChatData(mMessageId,
                                                                                SharedPreferenceEditor.getData(
                                                                                    USER_ID))
            if (listChatModel.isNotEmpty()) {
                if (listChatModel[0].messageStatus == ChatMessageStatus.NOT_SENT.ordinal) {
                    listChatModel[0].messageStatus = ChatMessageStatus.RETRY.ordinal
                    ChatApp.mAppDatabase!!.getChatDao().update(listChatModel[0])
                    if (str == "Single") {
                        Log.e("Nive ", "imageError: send event bus")
                        EventBus.getDefault().post("Image Error")
                    } else {
                        Log.e("Nive ", "imageError:Group")
                        EventBus.getDefault().post("Image Error Group")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Nive ", "imageError Exception: ${e.message}")
        }
    }
}