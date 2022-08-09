package com.sparkout.chat.common.repository

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.sparkout.chat.apiservice.ApiCall
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.model.CommonResponse
import com.sparkout.chat.network.ApiHelperImpl
import com.sparkout.chat.ui.apisample.BaseRepository
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named

class UploadMediaApiRepo @Inject constructor(var apiHelperImpl: ApiHelperImpl) : BaseRepository() {


    suspend fun uploadImageSus(imageBody: MultipartBody.Part) =
        apiHelperImpl.uploadImageSus(imageBody)

    suspend fun uploadImage(imageBody: MultipartBody.Part) =
        apiHelperImpl.uploadImage(imageBody)


    /*TODO: fun to set retry option*/
    private fun imageError(mMessageId: String, str: String) {
        try {
            val listChatModel = ChatApp.mAppDatabase!!.getChatDao().getChatData(
                mMessageId,
                SharedPreferenceEditor.getData(USER_ID)
            )
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