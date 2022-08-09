package com.sparkout.chat.ui.media.view.fragment

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.MediaClickOptions
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMediaTypes
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.exoplayer.view.ExoPlayerActivity
import com.sparkout.chat.ui.media.view.adapter.MediaAdapter
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class VideoFragment(private val mToId: String, private val mChatType: String) : Fragment(),
                                                                                MediaClickOptions {
    lateinit var rvVideos: RecyclerView
    lateinit var textViewNoVideos: AppCompatTextView
    lateinit var listChatModel: List<ChatModel>
    var listVideos = ArrayList<String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        rvVideos = view.findViewById(R.id.rv_videos)
        textViewNoVideos = view.findViewById(R.id.textview_no_videos)
        rvVideos.layoutManager = GridLayoutManager(requireContext(), 3)

        if (mChatType == ChatTypes.SINGLE.type) {
            listChatModel = ChatApp.mAppDatabase!!.getChatDao()
                .getMessageMedia(
                    SharedPreferenceEditor.getData(Global.USER_ID),
                                 mToId,
                                 ChatMessageTypes.VIDEO.type)
        } else if (mChatType == ChatTypes.GROUP.type) {
            listChatModel = ChatApp.mAppDatabase!!.getChatDao()
                .getGroupMessageMedia(
                    SharedPreferenceEditor.getData(Global.USER_ID),
                                      mToId,
                                      ChatMessageTypes.VIDEO.type)
        }
        val cw = ContextWrapper(requireContext())
        val folder = cw.getDir("Videos", Context.MODE_PRIVATE)
        val file = File(folder.absolutePath)
        loadVideos(file)
        val videoDirectoryPath =
            Environment.getExternalStorageDirectory().toString().plus("/")
                .plus(Environment.DIRECTORY_PICTURES).plus("/")
                .plus("Bee Bush Messenger Videos")
        val file1 = File(videoDirectoryPath)
        loadVideos(file1)

        return view
    }

    private fun loadVideos(file: File) {
        val listFiles = file.listFiles()
        if (null != listFiles) {
            for (i in listFiles.indices) {
                /*if (listFiles[i].isDirectory) {
                    loadVideos(listFiles[i])
                } else {

                }*/
                if (listChatModel.isNotEmpty()) {
                    for (j in listChatModel.indices) {
                        if (listFiles[i].name == (listChatModel[j].messageId)) {
                            if (!listVideos.contains(listFiles[i].absolutePath)) {
                                listVideos.add(listFiles[i].absolutePath)
                            }
                        } else {
                            if (!listVideos.contains(listChatModel[j].uri!!)) {
                                listVideos.add(listChatModel[j].uri!!)
                            }
                        }
                    }
                }
            }
            if (listVideos.isNotEmpty()) {
                textViewNoVideos.visibility = View.GONE
                rvVideos.visibility = View.VISIBLE
                val mediaAdapter =
                    MediaAdapter(requireContext(),
                                 listVideos,
                                 ChatMediaTypes.VIDEO.type.toInt(),
                                 this)
                rvVideos.adapter = mediaAdapter
            } else {
                rvVideos.visibility = View.GONE
                textViewNoVideos.visibility = View.VISIBLE
            }
        }
    }

    override fun mediaOptions(mString: String, mType: Int) {
        if (mType == ChatMediaTypes.VIDEO.type.toInt()) {
            startActivity(Intent(requireContext(), ExoPlayerActivity::class.java)
                              .putExtra("URL", mString))
        }
    }
}
