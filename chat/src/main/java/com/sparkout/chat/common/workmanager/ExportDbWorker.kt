package com.sparkout.chat.common.workmanager

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.ui.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 *Created by Nivetha S on 11-03-2021.
 */
class ExportDbWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    lateinit var mTableName: String

    override fun doWork(): Result {
        mTableName = inputData.getString("Table")!!

        exportDB(mTableName)
        return Result.success()
    }

    fun exportDB(mTableName: String) {
        try {
            // Query the data in the table
            val csvFile = File(Environment.getExternalStorageDirectory(), "BeeBushDB/")
            if (!csvFile.exists()) {
                csvFile.mkdir()
            }
            val cursor: Cursor;
            if (mTableName == "Chat") {
                cursor =
                    mAppDatabase!!.query("SELECT chatType, chatTime, messageStatus, messageId, messageType, checkEdit, checkReply, checkForwarded, replyMessageId, uri, sender, receiver, message, duration FROM $mTableName",
                                         null)
            } else {
                cursor = mAppDatabase!!.query("SELECT * FROM $mTableName", null)
            }
            val file = File(csvFile, "$mTableName.csv")
            file.createNewFile()
            val csvWriter = CSVWriter(FileWriter(file))
            // Write the table data to the file
            csvWriter.writeNext(cursor.columnNames)
            while (cursor.moveToNext()) {
                //Which column you want to exprort
                val arrStr =
                    arrayOfNulls<String>(cursor.columnCount)
                for (i in 0 until cursor.columnCount) {
                    if (mTableName == "User") {
                        if (cursor.getColumnName(i) == "checkFriend" || cursor.getColumnName(i) == "checkOffline") {
                            arrStr[i] = cursor.isNull(i).toString()
                        } else {
                            arrStr[i] = cursor.getString(i)
                        }
                    } else if (mTableName == "Chat") {
                        if (cursor.getColumnName(i) == "checkReply" || cursor.getColumnName(
                                i) == "checkEdit" || cursor.getColumnName(i) == "checkForwarded") {
                            arrStr[i] = cursor.isNull(i).toString()
                        } else {
                            arrStr[i] = cursor.getString(i)
                        }
                    } else if (mTableName == "GroupDetails") {
                        if (cursor.getColumnName(i) == "checkExit") {
                            arrStr[i] = cursor.isNull(i).toString()
                        } else {
                            arrStr[i] = cursor.getString(i)
                        }
                    } else if (mTableName == "GroupMember") {
                        if (cursor.getColumnName(i) == "checkRemoved" || cursor.getColumnName(i) == "checkAdmin") {
                            arrStr[i] = cursor.isNull(i).toString()
                        } else {
                            arrStr[i] = cursor.getString(i)
                        }
                    } else if (mTableName == "AudioStatus") {
                        if (cursor.getColumnName(i) == "checkPlayed") {
                            arrStr[i] = cursor.isNull(i).toString()
                        } else {
                            arrStr[i] = cursor.getString(i)
                        }
                    } else {
                        arrStr[i] = cursor.getString(i)
                    }
                }
                csvWriter.writeNext(arrStr)
            }
            Log.e("Nive ", "exportDB: " + mTableName)
            // close cursor and writer
            cursor.close()
            csvWriter.close()
        } catch (e: IOException) {
            // handle exception
        }
    }
}