package com.sparkout.chat.common.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sparkout.chat.common.BaseUtils

/**
 *Created by Nivetha S on 09-02-2021.
 */
class DownloadWorker(val mContext: Context, workerParameters: WorkerParameters) :
    Worker(mContext, workerParameters) {
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        val uri = inputData.getString("uri")
        val messageType = inputData.getString("messageType")
        val messageId = inputData.getString("messageId")
        val receiverId = inputData.getString("receiverId")
        Log.e("Nive ", "doWork: $uri")
        BaseUtils.download(mContext, uri!!, messageType!!, messageId!!, receiverId!!)
        return Result.Success()
    }
}