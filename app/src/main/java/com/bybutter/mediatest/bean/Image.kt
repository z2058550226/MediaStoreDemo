package com.bybutter.mediatest.bean

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.bybutter.mediatest.contentResolver

data class Image(
    val id: Long,
    val displayName: String,
    val uri: Uri,
    val size: Long,
    val cursorMimeType: String
) {
    val mediaStoreMimeType: String?
        get() = contentResolver.getType(
            ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
        )
}