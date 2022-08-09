package com.sparkout.chat.ui.media.view.fragment

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.MediaClickOptions
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMediaTypes
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.media.view.adapter.MediaAdapter
import com.sparkout.chat.ui.pinchzoom.view.ImageZoomActivity
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class PhotoFragment(private val mToId: String, private val mChatType: String) : Fragment(),
                                                                                MediaClickOptions {
    lateinit var rvPhotos: RecyclerView
    lateinit var textViewNoPhotos: AppCompatTextView
    lateinit var listChatModel: List<ChatModel>
    var listPhotos = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_photo, container, false)

        rvPhotos = view.findViewById(R.id.rv_photos)
        textViewNoPhotos = view.findViewById(R.id.textview_no_photos)
        rvPhotos.layoutManager = GridLayoutManager(requireContext(), 3)

        if (mChatType == ChatTypes.SINGLE.type) {
            listChatModel = mAppDatabase!!.getChatDao()
                .getMessageMedia(SharedPreferenceEditor.getData(Global.USER_ID), mToId, ChatMessageTypes.IMAGE.type)
        } else if (mChatType == ChatTypes.GROUP.type) {
            listChatModel = mAppDatabase!!.getChatDao()
                .getGroupMessageMedia(SharedPreferenceEditor.getData(Global.USER_ID),
                                      mToId,
                                      ChatMessageTypes.IMAGE.type)
        }
        val cw = ContextWrapper(requireContext())
        val folder = cw.getDir("Photos", Context.MODE_PRIVATE)
        val file = File(folder.absolutePath)
        loadPhotos(file)
        val imageDirectoryPath =
            Environment.getExternalStorageDirectory().toString().plus("/")
                .plus(Environment.DIRECTORY_PICTURES).plus("/")
                .plus("Bee Bush Messenger Photos")
        val file1 = File(imageDirectoryPath)
        loadPhotos(file1)

        return view
    }

    private fun loadPhotos(file: File) {
        val listFiles = file.listFiles()
        if (null != listFiles) {
            for (i in listFiles.indices) {
                if (listChatModel.isNotEmpty()) {
                    for (j in listChatModel.indices) {
                        if (listFiles[i].name == listChatModel[j].messageId) {
                            if (!listPhotos.contains(listFiles[i].absolutePath)) {
                                listPhotos.add(listFiles[i].absolutePath)
                            }
                        } else {
                            if (!listPhotos.contains(listChatModel[j].uri!!)) {
                                listPhotos.add(listChatModel[j].uri!!)
                            }
                        }
                    }
                }
            }

            if (listPhotos.isNotEmpty()) {
                textViewNoPhotos.visibility = View.GONE
                rvPhotos.visibility = View.VISIBLE
                val mediaAdapter =
                    MediaAdapter(requireContext(),
                                 listPhotos,
                                 ChatMediaTypes.PHOTO.type.toInt(),
                                 this)
                rvPhotos.adapter = mediaAdapter
            } else {
                rvPhotos.visibility = View.GONE
                textViewNoPhotos.visibility = View.VISIBLE
            }
        }
    }

    override fun mediaOptions(mString: String, mType: Int) {
        if (mType == ChatMediaTypes.PHOTO.type.toInt()) {
            if (mString.contains(".jpeg")) {
                startActivity(Intent(requireContext(), ImageZoomActivity::class.java)
                                  .putExtra("URL", mString)
                                  .putExtra("value", 2))
            } else {
                startActivity(Intent(requireContext(), ImageZoomActivity::class.java)
                                  .putExtra("PATH", mString)
                                  .putExtra("value", mType))
            }
        }
    }
}
