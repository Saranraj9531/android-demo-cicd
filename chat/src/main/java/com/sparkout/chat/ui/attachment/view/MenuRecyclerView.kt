package com.sparkout.chat.ui.attachment.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sparkout.chat.ui.attachment.decoration.MenuGridDecoration

class MenuRecyclerView : RecyclerView {
    private val spanCount = 3
    private val manager = GridLayoutManager(context, spanCount)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
                                                                               attrs,
                                                                               defStyle)

    init {
        setHasFixedSize(true)
        layoutManager = manager
        addItemDecoration(MenuGridDecoration())
    }
}