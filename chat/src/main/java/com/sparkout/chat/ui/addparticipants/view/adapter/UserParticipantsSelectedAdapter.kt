package com.sparkout.chat.ui.addparticipants.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.databinding.ItemSelectedParticipantBinding
import com.sparkout.chat.ui.addparticipants.view.AddParticipantsActivity

class UserParticipantsSelectedAdapter(
    val mContext: Context,
    private val mListParticipantsSelected: ArrayList<UserDetailsModel>
) :
    RecyclerView.Adapter<UserParticipantsSelectedAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding =
            ItemSelectedParticipantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return mListParticipantsSelected.size
    }

    override fun onBindViewHolder(
        holder: UserParticipantsSelectedAdapter.ViewHolder,
        position: Int
    ) {

        holder.binding.textviewContactName.text = mListParticipantsSelected[position].username
        Glide.with(mContext)
            .setDefaultRequestOptions(requestOptionsD()!!)
            .load(mListParticipantsSelected[position].profilePicture)
            .into(holder.binding.imageviewSelectedContact)
    }

    inner class ViewHolder(var binding: ItemSelectedParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.imageviewRemoveContact.setOnClickListener { v ->
                BaseUtils.preventDoubleClick(v!!)
                (mContext as AddParticipantsActivity).refershParticipants(
                    mListParticipantsSelected[absoluteAdapterPosition]
                )
            }
        }
    }
}