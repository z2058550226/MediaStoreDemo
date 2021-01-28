package com.bybutter.mediatest.bean

import android.net.Uri

data class Bucket(
    val bucketId: Long, // bucket id, not _ID
    val displayName: String?,
    val bucketUri: Uri,
    val mimeType: String?,
    val mediaType: Int,
    var itemCount: Int = 0,
    val id: Long = -1
)