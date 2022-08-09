package com.sparkout.chat.common

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// Created by krish on 18-Aug-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
class RecyclerViewItemClickListener(val context: Context,
                                    val recyclerView: RecyclerView,
                                    val listener: OnItemClickListener) :
    RecyclerView.OnItemTouchListener {
    companion object {
        interface OnItemClickListener {
            fun onItemClick(view: View?, position: Int)
            fun onItemLongClick(view: View?, position: Int)
        }
    }

    private val mGestureDetector: GestureDetector =
        GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                try {
                    val childView: View = recyclerView.findChildViewUnder(e.x, e.y)!!
                    if (childView != null && listener != null) {
                        listener.onItemLongClick(childView,
                                                 recyclerView.getChildLayoutPosition(childView))
                    }
                } catch (e: Exception) {
                    Log.e("Nive ", "onLongPress: ${e.message}")
                }
            }
        })

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        try {
            if (null != view) {
                val childView: View = view.findChildViewUnder(e.x, e.y)!!

                if (childView != null && listener != null && mGestureDetector.onTouchEvent(e)) {
                    listener.onItemClick(childView, view.getChildLayoutPosition(childView))
                }
            }
        } catch (e: Exception) {
        }

        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}