package com.sparkout.chat.ui.creategroup.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.databinding.ItemParticipantContactBinding

class GroupParticipantsAdapter(
    private val mContext: Context,
    private val mListGroupParticipants: ArrayList<UserDetailsModel>
) :
    RecyclerView.Adapter<GroupParticipantsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemParticipantContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return mListGroupParticipants.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textviewContactName.text = mListGroupParticipants[position].username
        Glide.with(mContext)
            .setDefaultRequestOptions(requestOptionsD()!!)
            .load(mListGroupParticipants[position].profilePicture)
            .into(holder.binding.imageviewContact)
    }

    inner class ViewHolder(var binding: ItemParticipantContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}