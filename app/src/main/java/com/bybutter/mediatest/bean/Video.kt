package com.bybutter.mediatest.bean

import android.net.Uri

data class Video(
    val id: Long,
    val displayName: String,
    val uri: Uri,
    val size: Long
)