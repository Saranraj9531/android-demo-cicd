package com.sparkout.chat.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sparkout.chat.R

class ItemClickSupport(view: RecyclerView) {
    var recyclerView: RecyclerView = view
    private lateinit var mOnItemClickListener: OnItemClickListener

    init {
        recyclerView.setTag(R.id.item_click_support, this)
        recyclerView.addOnChildAttachStateChangeListener(object :
                                                             RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {
            }

            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnClickListener(mOnClickListener)
            }
        })
    }

    private val mOnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            // ask the RecyclerView for the viewHolder of this view.
            // then use it to get the position for the adapter
            val holder = recyclerView.getChildViewHolder(v)
            mOnItemClickListener.onItemClicked(recyclerView, holder.adapterPosition, v)
        }
    }
    private val mAttachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            // every time a new child view is attached add click listeners to it
            view.setOnClickListener(mOnClickListener)
        }

        override fun onChildViewDetachedFromWindow(view: View) {
        }
    }

    companion object {
        fun addTo(view: RecyclerView): ItemClickSupport {
            // if there's already an itemClickSupport attached
            // to this RecyclerView do not replace it, use it
            var support: ItemClickSupport? =
                view.getTag(R.id.item_click_support) as? ItemClickSupport
            if (support == null) {
                support = ItemClickSupport(view)
            }
            return support
        }
    }

    fun removeFrom(view: RecyclerView): ItemClickSupport {
        val support = view.getTag(R.id.item_click_support) as ItemClickSupport
        support.detach(view)
        return support
    }

    fun setOnItemClickListener(listener: OnItemClickListener): ItemClickSupport {
        mOnItemClickListener = listener
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.item_click_support, null)
    }

    interface OnItemClickListener {
        fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View)
    }
}