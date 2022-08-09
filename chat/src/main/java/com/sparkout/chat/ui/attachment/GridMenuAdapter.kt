package com.sparkout.chat.ui.attachment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.databinding.ItemMenuBinding

class GridMenuAdapter(menus: ArrayList<Menu>) :
    RecyclerView.Adapter<GridMenuAdapter.MenuViewHolder>() {
    interface GridMenuListener {
        fun dismissPopup()
    }

    private val data = ArrayList<Menu>().apply {
        addAll(menus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder.create(
            parent,
            viewType
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.biding.textviewTitle.text = data[position].name
        holder.biding.imageviewIcon.setImageDrawable(
            ContextCompat.getDrawable(
                ChatApp.mInstance,
                data[position].drawable
            )
        )
    }

    class MenuViewHolder(var biding: ItemMenuBinding) : RecyclerView.ViewHolder(biding.root) {


        companion object {

            fun create(parent: ViewGroup, viewType: Int): MenuViewHolder {
                val binding =
                    ItemMenuBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return MenuViewHolder(binding)
            }
        }
    }

    data class Menu(val name: String, @DrawableRes val drawable: Int) {
    }
}