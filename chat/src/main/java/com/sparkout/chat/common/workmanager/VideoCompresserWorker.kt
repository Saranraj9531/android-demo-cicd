package com.sparkout.chat.common.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMessageStatus
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.VideoCompressModel
import com.sparkout.chat.common.videocompres.VideoCompress
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.chat.view.ChatActivity
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 *Created by Nivetha S on 24-03-2021.
 */
class VideoCompresserWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        var path = inputData.getString("video_path")
        var mMessageId = inputData.getString("message_id")

        if (path!!.isNotEmpty()) {
            Log.e("Nive ", "onActivityResult:galleryvedio ${path}")
            try {
                val videoPathList =
                    java.util.ArrayList<String>()
                val cw = ContextWrapper(applicationContext)
                val folder = cw.getDir("Videos", Context.MODE_PRIVATE)
                val sourceLocation = File(path)
                val targetLocation =
                    File(folder, mMessageId!! + ".mp4")
                VideoCompress.compressVideoLow(sourceLocation.absolutePath,
                                               targetLocation.absolutePath,
                                               object : VideoCompress.CompressListener {
                                                   override fun onSuccessCompress() {
                                                       Log.e("Nive ", "onSuccess:compress ")
                                                       videoPathList.add(targetLocation.absolutePath)
                                                       Log.e("Nive ",
                                                             "doWork:VideoPath " + videoPathList[0])
                                                       val mFileSize =
                                                           targetLocation.length() / (1024 * 1024)
                                                       Log.e("Nive ",
                                                             "onSuccess:Compresed $mFileSize")
                                                       if (mFileSize <= 100) {
                                                           if (videoPathList.isNotEmpty()) {
                                                               EventBus.getDefault()
                                                                   .post(VideoCompressModel(
                                                                       videoPathList[0],
                                                                       mMessageId))
                                                           } else {
                                                               BaseUtils.showToast(ChatApp.mInstance,
                                                                                   "Unable to upload file")
                                                           }
                                                       } else {
                                                           val listChatUpdate =
                                                               ChatApp.mAppDatabase!!.getChatDao()
                                                                   .getChatData(mMessageId,
                                                                                SharedPreferenceEditor.getData(USER_ID))
                                                           if (listChatUpdate.isNotEmpty()) {
                                                               ChatApp.mAppDatabase!!.getChatDao()
                                                                   .delete(listChatUpdate[0])
                                                               EventBus.getDefault()
                                                                   .post("upload_video_failed")
                                                           }

                                                           BaseUtils.showToast(ChatApp.mInstance,
                                                                               "Maximum file size upload restricted to 100 mb")
                                                       }
                                                   }

                                                   override fun onFailCompress() {
                                                   }

                                                   override fun onProgressCompress(percent: Float) {
                                                       Log.e("Nive ",
                                                             "onProgress:VideoProgress $percent")
                                                   }

                                                   override fun onStartCompress() {
                                                   }
                                               })
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return Result.Success()
    }
}