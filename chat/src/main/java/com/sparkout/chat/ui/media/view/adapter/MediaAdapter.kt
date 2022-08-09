package com.sparkout.chat.ui.media.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sparkout.chat.R
import com.sparkout.chat.clickinterface.MediaClickOptions
import com.sparkout.chat.common.BaseUtils.Companion.pickImageFromStorage
import com.sparkout.chat.common.BaseUtils.Companion.requestOptionsT
import com.sparkout.chat.common.chatenum.ChatMediaTypes
import java.util.*

// Created by krish on 11-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class MediaAdapter(val context: Context,
                   private val listMedia: ArrayList<String>,
                   private val mItemType: Int,
                   val mediaClickOptions: MediaClickOptions) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        val viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ChatMediaTypes.PHOTO.type.toInt()    -> {
                view = inflater.inflate(R.layout.item_media_photos, parent, false)
                return PhotoViewHolder(view)
            }
            ChatMediaTypes.VIDEO.type.toInt()    -> {
                view = inflater.inflate(R.layout.item_media_video, parent, false)
                return VideoViewHolder(view)
            }
            ChatMediaTypes.DOCUMENT.type.toInt() -> {
                view = inflater.inflate(R.layout.item_media_document, parent, false)
                return DocumentViewHolder(view)
            }
        }
        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return listMedia.size
    }

    override fun getItemViewType(position: Int): Int {
        return mItemType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ChatMediaTypes.PHOTO.type.toInt()    -> {
                val photoViewHolder = holder as PhotoViewHolder
                Glide.with(context)
                    .setDefaultRequestOptions(requestOptionsT()!!)
                    .load(listMedia[position])
                    .into(photoViewHolder.imageViewPhoto)

                photoViewHolder.imageViewPhoto.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        mediaClickOptions.mediaOptions(listMedia[position],
                                                       ChatMediaTypes.PHOTO.type.toInt())
                    }
                })
            }
            ChatMediaTypes.VIDEO.type.toInt()    -> {
                val videoViewHolder = holder as VideoViewHolder
                if (null != pickImageFromStorage(context,
                                                 listMedia[position],
                                                 ChatMediaTypes.VIDEO.type.toInt())) {
                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(pickImageFromStorage(context,
                                                   listMedia[position],
                                                   ChatMediaTypes.VIDEO.type.toInt()))
                        .into(videoViewHolder.imageViewVideo)
                } else {
                    Glide.with(context)
                        .setDefaultRequestOptions(requestOptionsT()!!)
                        .load(listMedia[position])
                        .into(videoViewHolder.imageViewVideo)
                }
                videoViewHolder.cardViewVideo.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        if (null != pickImageFromStorage(context,
                                                         listMedia[position],
                                                         ChatMediaTypes.VIDEO.type.toInt())) {
                            mediaClickOptions.mediaOptions(pickImageFromStorage(context,
                                                                                listMedia[position],
                                                                                ChatMediaTypes.VIDEO.type.toInt())!!,
                                                           ChatMediaTypes.VIDEO.type.toInt())
                        } else {
                            mediaClickOptions.mediaOptions(listMedia[position],
                                                           ChatMediaTypes.VIDEO.type.toInt())
                        }
                    }
                })
            }
            ChatMediaTypes.DOCUMENT.type.toInt() -> {
                val documentViewHolder = holder as DocumentViewHolder
                documentViewHolder.cardViewDocument.setOnClickListener(object :
                                                                           View.OnClickListener {
                    override fun onClick(v: View?) {
                        mediaClickOptions.mediaOptions(listMedia[position],
                                                       ChatMediaTypes.DOCUMENT.type.toInt())
                    }
                })
            }
        }
    }

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewPhoto: AppCompatImageView = itemView.findViewById(R.id.imageview_photo)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardViewVideo: CardView = itemView.findViewById(R.id.card_video)
        var imageViewVideo: AppCompatImageView = itemView.findViewById(R.id.imageview_video)
    }

    class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardViewDocument: CardView = itemView.findViewById(R.id.card_document)
    }
}