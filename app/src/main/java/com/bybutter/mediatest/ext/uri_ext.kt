package com.bybutter.mediatest.ext

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import com.bybutter.mediatest.app
import com.bybutter.mediatest.contentResolver
import com.bybutter.mediatest.other.ContentUri
import java.io.File
import java.net.URLConnection

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

private const val MEDIA_VIDEO_DIR = "SuikaVideo"
private const val MEDIA_PICTURE_DIR = "SuikaPicture"

/**
 * Create image file by [MediaStore]
 *
 * @receiver should be a filename with extension, but without parent path
 */
@ContentUri
fun String.insertPictureToMediaStore(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        insertQ(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_PICTURES, MEDIA_PICTURE_DIR)
    } else {
        insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_PICTURES, MEDIA_PICTURE_DIR)
    }
}

/**
 * Create video file by [MediaStore]
 *
 * @receiver should be a filename with extension, but without parent path
 */
@ContentUri
fun String.insertVideoToMediaStore(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        insertQ(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_MOVIES, MEDIA_VIDEO_DIR)
    } else {
        insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Environment.DIRECTORY_MOVIES, MEDIA_VIDEO_DIR)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun String.insertQ(url: Uri, bucketName: String, subDir: String): Uri? {
    return contentResolver.insert(url, contentValuesOf(
        MediaStore.MediaColumns.DISPLAY_NAME to this,
        MediaStore.MediaColumns.MIME_TYPE to URLConnection.guessContentTypeFromName(this),
        MediaStore.MediaColumns.RELATIVE_PATH to "$bucketName/$subDir",
        MediaStore.MediaColumns.DATE_MODIFIED to System.currentTimeMillis() / 1000
    ))
}

private fun String.insert(url: Uri, bucketName: String, subDir: String): Uri? {
    @Suppress("DEPRECATION") val parentPath =
        "${Environment.getExternalStoragePublicDirectory(bucketName).absolutePath}/$subDir"
    val parentDir = File(parentPath)
    if (parentDir.exists().not()) parentDir.mkdirs()

    val file = File(parentDir, this)
    if (file.exists().not()) {
        file.createNewFile()
        return contentResolver.insert(url, contentValuesOf(
            MediaStore.MediaColumns.DATA to file.absolutePath,
            MediaStore.MediaColumns.DISPLAY_NAME to this,
            MediaStore.MediaColumns.MIME_TYPE to URLConnection.guessContentTypeFromName(this),
            MediaStore.MediaColumns.DATE_MODIFIED to System.currentTimeMillis() / 1000
        ))
    } else {
        val id: Long? = contentResolver.query(url, arrayOf(BaseColumns._ID), "${MediaStore.MediaColumns.DATA}=?", arrayOf(file.absolutePath), null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return@use cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
            }
            return@use null
        }
        return id?.let { ContentUris.withAppendedId(url, it) }
    }
}

fun Uri.saveToUri(uri: Uri) {
    contentResolver.openInputStream(this)?.use { input ->
        contentResolver.openOutputStream(uri)?.use { output ->
            input.copyTo(output)
        }
    }
}