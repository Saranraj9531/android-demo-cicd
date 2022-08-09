package com.sparkout.chat.ui.addparticipants.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.model.UserDetailsModel
import com.sparkout.chat.databinding.ItemUserParticipantBinding
import com.sparkout.chat.ui.addparticipants.view.AddParticipantsActivity

class UserParticipantsAdapter(
    val mContext: Context,
    private val mListUsers: ArrayList<UserDetailsModel>,
    private val mListSelectedUsers: ArrayList<UserDetailsModel>
) :
    RecyclerView.Adapter<UserParticipantsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemUserParticipantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mListUsers.size
    }

    override fun onBindViewHolder(holder: UserParticipantsAdapter.ViewHolder, position: Int) {
        Glide.with(mContext)
            .setDefaultRequestOptions(requestOptionsD()!!)
            .load(mListUsers[position].profilePicture)
            .into(holder.binding.imageviewContact)
        holder.binding.textviewContactName.text = mListUsers[position].username
        val mListUser = ArrayList<String>()

        if (mListSelectedUsers.isNotEmpty()) {
            for (i in 0 until mListSelectedUsers.size) {
                mListUser.add(mListSelectedUsers[i].id)
            }
        }

        holder.binding.btnSelect.isChecked = mListUser.contains(mListUsers[position].id)
    }

    inner class ViewHolder(var binding: ItemUserParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnSelect.setOnClickListener {
                if (binding.btnSelect.isChecked) {
                    binding.btnSelect.isChecked = true
                    (binding as AddParticipantsActivity).triggerSelectedParticipants(
                        mListUsers[absoluteAdapterPosition]
                    )
                } else {
                    binding.btnSelect.isChecked = false
                    (mContext as AddParticipantsActivity).refreshSelectedParticipants(
                        mListUsers[absoluteAdapterPosition]
                    )
                }
            }
            binding.layoutContactList.setOnClickListener { v ->
                BaseUtils.preventDoubleClick(v!!)
                if (!binding.btnSelect.isChecked) {
                    binding.btnSelect.isChecked = true
                    (mContext as AddParticipantsActivity).triggerSelectedParticipants(
                        mListUsers[absoluteAdapterPosition]
                    )
                } else {
                    binding.btnSelect.isChecked = false
                    (mContext as AddParticipantsActivity).refreshSelectedParticipants(
                        mListUsers[absoluteAdapterPosition]
                    )
                }
            }
        }
    }
}