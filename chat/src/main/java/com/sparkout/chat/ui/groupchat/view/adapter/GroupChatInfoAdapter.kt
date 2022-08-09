package com.sparkout.chat.ui.groupchat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.databinding.ItemDeliverSeenMembersBinding

class GroupChatInfoAdapter(
    val context: Context,
    private val mListGroupChatInfo: List<String>
) :
    RecyclerView.Adapter<GroupChatInfoAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupChatInfoAdapter.ViewHolder {
        val binding =
            ItemDeliverSeenMembersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return mListGroupChatInfo.size
    }

    override fun onBindViewHolder(holder: GroupChatInfoAdapter.ViewHolder, position: Int) {

        val mListUserDetails =
            mAppDatabase!!.getUserDetailsDao().getUserDetails(mListGroupChatInfo[position])
        if (mListUserDetails.isNotEmpty()) {
            holder.itemDeliverSeenMembersBinding.textviewDeliverSeenName.text =
                mListUserDetails[0].username
            Glide.with(context)
                .setDefaultRequestOptions(requestOptionsD()!!)
                .load(mListUserDetails[0].profilePicture)
                .into(holder.itemDeliverSeenMembersBinding.imageviewDeliverSeenMember)

        }
    }

    class ViewHolder(var itemDeliverSeenMembersBinding: ItemDeliverSeenMembersBinding) :
        RecyclerView.ViewHolder(itemDeliverSeenMembersBinding.root) {
    }
}