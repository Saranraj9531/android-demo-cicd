package com.sparkout.chat.ui.forward.view.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.common.chatenum.ChatTypes
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.ui.chat.model.ChatListModel
import com.sparkout.chat.ui.forward.view.ForwardActivity
import com.sparkout.chat.ui.forward.view.adapter.ForwardAdapter.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

// Created by krish on 19-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class ForwardAdapter(val context: Context, private var listChatForward: List<ChatListModel>) :
    RecyclerView.Adapter<ViewHolder>() {
    private var chatAdapterFilterList: ArrayList<ChatListModel>
    private var mSubjectForwardDataFilter: SubjectForwardDataFilter? = null

    init {
        chatAdapterFilterList = ArrayList<ChatListModel>()
        chatAdapterFilterList.addAll(listChatForward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forward_contact, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatAdapterFilterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatListPojo: ChatListModel = chatAdapterFilterList[position]
        if (chatListPojo.chatType == ChatTypes.SINGLE.type) {
            holder.textViewForwardName.text = chatListPojo.name
            Glide.with(context).setDefaultRequestOptions(requestOptionsD()!!)
                .load(chatListPojo.profilePicture)
                .into(holder.imageViewForwardContact)
        } else {
            val listGroupDetails = mAppDatabase!!.getGroupDetailsDao()
                .getGroupDetails(chatListPojo.receiverId,
                    SharedPreferenceEditor.getData(Global.USER_ID))
            if (listGroupDetails.isNotEmpty()) {
                holder.textViewForwardName.text = listGroupDetails[0].groupTitle
                Glide.with(context)
                    .setDefaultRequestOptions(BaseUtils.glideRequestOptionProfile())
                    .load(listGroupDetails[0].groupImage)
                    .into(holder.imageViewForwardContact)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutForwardContact: RelativeLayout =
            itemView.findViewById<RelativeLayout>(R.id.layout_forward_contact)
        val imageViewForwardContact: CircleImageView =
            itemView.findViewById<CircleImageView>(R.id.imageview_forward_contact)
        val textViewForwardName: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.textview_forward_name)

        init {
            this.setIsRecyclable(false)

            layoutForwardContact.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    (context as ForwardActivity).selectedContact(chatAdapterFilterList[adapterPosition])
                }
            })
        }
    }

    fun getFilter(): Filter? {
        if (mSubjectForwardDataFilter == null) {
            mSubjectForwardDataFilter = SubjectForwardDataFilter()
        }
        return mSubjectForwardDataFilter
    }

    inner class SubjectForwardDataFilter : Filter() {
        override fun performFiltering(mCharSequence: CharSequence): FilterResults {
            var charSequence: CharSequence? = mCharSequence
            charSequence = charSequence.toString().toLowerCase(Locale.getDefault())
            val filterResults = FilterResults()
            if (charSequence != null && charSequence.toString().isNotEmpty()) {
                val arrayList1: MutableList<ChatListModel> = java.util.ArrayList<ChatListModel>()

                for (i in 0 until listChatForward.size) {
                    val mChatListModel = listChatForward[i]
                    if (mChatListModel.name.toString().toLowerCase().contains(charSequence)) {
                        arrayList1.add(mChatListModel)
                    }
                }

                filterResults.count = arrayList1.size
                filterResults.values = arrayList1
            } else {
                synchronized(this) {
                    filterResults.values = listChatForward
                    filterResults.count = listChatForward.size
                }
            }
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence,
                                    filterResults: FilterResults) {
            chatAdapterFilterList = filterResults.values as java.util.ArrayList<ChatListModel>
            notifyDataSetChanged()
        }
    }
}