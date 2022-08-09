package com.sparkout.chat.common

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.common.util.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// Created by krish on 24-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ImageFilePath {
    companion object {
        /**
         * Method for return file path of Gallery image
         *
         * @param context
         * @param uri
         * @return path of the selected image file from gallery
         */
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        fun getFile(context: Context, uri: Uri): File? {
            if (DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return File(Environment.getExternalStorageDirectory()
                                        .toString() + "/" + split[1])
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id))
                    return File(getDataColumn(context, contentUri, null, null)!!)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1])
                    return File(getDataColumn(context, contentUri, selection, selectionArgs)!!)
                }
            } else if ("content".equals(uri.scheme,
                                        ignoreCase = true)
            ) { // Return the remote address
                return if (isGooglePhotosUri(uri)) File(uri.lastPathSegment!!) else File(
                    getDataColumn(context,
                                  uri,
                                  null,
                                  null)!!)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return File(uri.path!!)
            }
            return null
        }

        @SuppressLint("NewApi")
        fun getPath(context: Context, uri: Uri): String? {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    return if ("primary".equals(type, ignoreCase = true)) {
                        Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    } else {
                        var filePath: String? = null
                        if (Build.VERSION.SDK_INT > 20) {
                            val extenal = context.externalMediaDirs
                            if (extenal.size > 1) {
                                filePath = extenal[1].absolutePath
                                filePath =
                                    filePath.substring(0, filePath.indexOf("Android")) + split[1]
                            }
                        } else {
                            filePath = "/storage/" + type + "/" + split[1]
                        }
                        filePath
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (!TextUtils.isEmpty(id)) {
                        return if (id.startsWith("raw:")) {
                            id.replaceFirst("raw:".toRegex(), "")
                        } else try {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id))
                            getDataColumn(context, contentUri, null, null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                                               )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context,
                                                                                          uri,
                                                                                          null,
                                                                                          null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        fun getDataColumn(context: Context,
                          uri: Uri?,
                          selection: String?,
                          selectionArgs: Array<String>?): String? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val cursor: Cursor =
                    context.contentResolver.query(uri!!, null, null, null, null)!!
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                val filename = cursor.getString(nameIndex)
                val filesize = cursor.getLong(sizeIndex)
                Log.e("Nive ", "getDataColumn:list $filename")
                val isIn: InputStream = context.contentResolver.openInputStream(uri)!!
                val cw = ContextWrapper(context)
                val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
                val file = File(folder, filename)
                val out = FileOutputStream(file)
                IOUtils.copyStream(isIn, out)
                Log.e("Nive ", "getDataColumn:AbsolutePath " + file.absolutePath)
                return file.absolutePath
            } else {
                Log.e("Nive ", "getDataColumn: else ")
                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)
                try {
                    cursor =
                        context.contentResolver.query(uri!!,
                                                      projection,
                                                      selection,
                                                      selectionArgs,
                                                      null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        return cursor.getString(index)
                    }
                } finally {
                    cursor?.close()
                }
                return null
            }
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }
}