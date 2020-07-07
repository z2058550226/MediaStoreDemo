package com.bybutter.mediatest.bean

import android.net.Uri

data class Bucket(
    val id: Long, // bucket id, not _ID
    val displayName: String?,
    val bucketUri: Uri,
    val mimeType: String?,
    val mediaType: Int
)