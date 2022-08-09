package com.sparkout.chat.ui.media.view.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.MediaClickOptions
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatMediaTypes
import com.sparkout.chat.common.chatenum.ChatMessageTypes
import com.sparkout.chat.ui.chat.model.ChatModel
import com.sparkout.chat.ui.media.view.adapter.MediaAdapter
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class DocumentFragment(private val mToId: String) : Fragment(), MediaClickOptions {
    lateinit var rvDocuments: RecyclerView
    lateinit var textViewNoDocuments: AppCompatTextView
    lateinit var listChatModel: List<ChatModel>
    var listDocument = ArrayList<String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_document, container, false)

        rvDocuments = view.findViewById(R.id.rv_documents)
        textViewNoDocuments = view.findViewById(R.id.textview_no_documents)
        rvDocuments.layoutManager = LinearLayoutManager(requireContext())

        listChatModel = ChatApp.mAppDatabase!!.getChatDao()
            .getMessageMedia(
                SharedPreferenceEditor.getData(Global.USER_ID),
                             mToId,
                             ChatMessageTypes.DOCUMENT.type)
        val file = File(BaseUtils.sDocumentDirectoryPath)
        loadDocument(file)

        return view
    }

    private fun loadDocument(file: File) {
        listDocument.clear()
        if (listChatModel.isNotEmpty()) {
            for (i in listChatModel.indices) {
                listDocument.add(listChatModel[i].uri.toString())
            }
        }
        if (listDocument.isNotEmpty()) {
            textViewNoDocuments.visibility = View.GONE
            rvDocuments.visibility = View.VISIBLE
            val mediaAdapter =
                MediaAdapter(requireContext(),
                             listDocument,
                             ChatMediaTypes.DOCUMENT.type.toInt(),
                             this)
            rvDocuments.adapter = mediaAdapter
        } else {
            rvDocuments.visibility = View.GONE
            textViewNoDocuments.visibility = View.VISIBLE
        }
    }

    override fun mediaOptions(mString: String, mType: Int) {
        if (mType == ChatMediaTypes.DOCUMENT.type.toInt()) {
            val target = Intent(Intent.ACTION_VIEW)
            target.setDataAndType(Uri.fromFile(File(mString)), "application/pdf")
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            val intent = Intent.createChooser(target, "Open File")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
    }
}
