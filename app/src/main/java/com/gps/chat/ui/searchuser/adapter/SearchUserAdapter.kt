package com.gps.chat.ui.searchuser.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gps.chat.databinding.ItemSearchUserBinding
import com.gps.chat.utils.glideRequestOptionProfile
import com.sparkout.chat.common.model.UserDetailsModel

class SearchUserAdapter(
    private val activity: Context,
    private val listSearchedUser: ArrayList<UserDetailsModel>,
    private val onItemClicked: ((UserDetailsModel) -> Unit)

) :
    RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val binding =
            ItemSearchUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listSearchedUser.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textViewFullname.text = listSearchedUser[position].name
        holder.binding.textViewUsername.text = listSearchedUser[position].username
        Glide.with(activity)
            .setDefaultRequestOptions(glideRequestOptionProfile())
            .load(listSearchedUser[position].profilePicture)
            .into(holder.binding.imageViewContactPicture)

        holder.itemView.setOnClickListener {
            onItemClicked.invoke(listSearchedUser[position])
        }
    }

    class ViewHolder(var binding: ItemSearchUserBinding) : RecyclerView.ViewHolder(binding.root)


}