package com.sparkout.chat.musicplayer.common

import com.sparkout.chat.musicplayer.media.Media

data class QueueData(

    val media: List<Media>,
    val index: Int

)