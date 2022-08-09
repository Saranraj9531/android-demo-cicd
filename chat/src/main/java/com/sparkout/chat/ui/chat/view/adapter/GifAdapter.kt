package com.sparkout.chat.ui.chat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifOptions
import com.bumptech.glide.request.RequestOptions
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.GifClickOptions
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsT
import com.sparkout.chat.ui.chat.model.GifResponse
import java.util.*

// Created by krish on 17-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class GifAdapter(val context: Context,
                 private val listGif: ArrayList<GifResponse.GifData>,
                 val gifClickOptions: GifClickOptions) :
    RecyclerView.Adapter<GifAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gif, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listGif.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .setDefaultRequestOptions(requestOptionsT()!!)
            .asGif()
            .apply(RequestOptions().set(GifOptions.DISABLE_ANIMATION, false))
            .load(listGif[position].images.original.url)
            .into(holder.imageViewGif)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewGif: AppCompatImageView =
            itemView.findViewById<AppCompatImageView>(R.id.imageview_gif_item)

        init {
            this.setIsRecyclable(false)

            imageViewGif.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    gifClickOptions.gifOptions(listGif[adapterPosition])
                }
            })
        }
    }
}