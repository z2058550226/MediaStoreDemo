package com.bybutter.mediatest.bean

import android.net.Uri

data class Image(
    val id: Long,
    val displayName: String,
    val uri: Uri,
    val size: Long
)