package com.sparkout.chat.ui.groupchat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.R
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsD
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.ChatApp.Companion.mAppDatabase
import com.sparkout.chat.common.Global
import com.sparkout.chat.common.Global.PROFILE_IMAGE
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.databinding.ItemGroupMembersBinding
import com.sparkout.chat.ui.groupchat.model.GroupMemberModel
import com.sparkout.chat.ui.groupinfo.view.GroupInfoActivity

class GroupMemberAdapter(
    val context: Context,
    private val mGroupMemberList: List<GroupMemberModel>
) :
    RecyclerView.Adapter<GroupMemberAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupMemberAdapter.ViewHolder {
        val binding =
            ItemGroupMembersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)

    }

    override fun getItemCount(): Int {
        return mGroupMemberList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mGroupMemberList[position].checkAdmin) {
            holder.binding.textviewAdmin.visibility = View.VISIBLE
        }
        if (mGroupMemberList[position].memberId == SharedPreferenceEditor.getData(Global.USER_ID)) {
            holder.binding.textviewMemberName.text =
                context.resources.getString(R.string.str_you)
            Glide.with(context)
                .setDefaultRequestOptions(requestOptionsD()!!)
                .load(SharedPreferenceEditor.getData(PROFILE_IMAGE))
                .into(holder.binding.imageviewGroupMember)
        } else {
            val mUserDetailsList = mAppDatabase!!.getUserDetailsDao()
                .getUserDetails(mGroupMemberList[position].memberId)
            if (mUserDetailsList.isNotEmpty()) {
                holder.binding.textviewMemberName.text = mUserDetailsList[0].username
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsD()!!)
                    .load(mUserDetailsList[0].profilePicture)
                    .into(holder.binding.imageviewGroupMember)
            } else {
                holder.binding.textviewMemberName.text = "Not your Friend"
            }
        }
    }

    inner class ViewHolder(var binding: ItemGroupMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.layoutGroupMembers.setOnLongClickListener {
                val mGroupAdminList = mAppDatabase!!.getGroupMemberDao()
                    .getCheckAdmin(
                        mGroupMemberList[adapterPosition].groupId,
                        SharedPreferenceEditor.getData(Global.USER_ID)
                    )
                if (mGroupAdminList.isNotEmpty() && mGroupMemberList[adapterPosition].memberId !=
                    SharedPreferenceEditor.getData(Global.USER_ID)
                ) {
                    (context as GroupInfoActivity).popupMenu(
                        binding.layoutGroupMembers,
                        mGroupMemberList[adapterPosition]
                    )
                }
                false
            }
        }
    }
}