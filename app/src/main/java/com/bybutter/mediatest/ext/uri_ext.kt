package com.bybutter.mediatest.ext

import android.net.Uri
import android.provider.MediaStore
import com.bybutter.mediatest.app

fun Uri.mediaQuery(
    projection: Array<String>?,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null
) = app.contentResolver.query(this, projection, selection, selectionArgs, sortOrder)
    ?.use { cursor ->
        val list = mutableListOf<List<String>>()
        val indexMax = (projection?.size ?: 30) - 1
        while (cursor.moveToNext()) {
            val sublist = mutableListOf<String>()
            for (i in 0..indexMax) {
                sublist.add(cursor.getString(i).orEmpty())
            }
            list.add(sublist)
        }
        list
    }