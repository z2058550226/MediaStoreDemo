package com.bybutter.mediatest.ext

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.bybutter.mediatest.app
import java.io.File

fun getFullPathFromContentUri(context: Context, uri: Uri): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if ("com.android.externalstorage.documents" == uri.authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            // TODO handle non-primary volumes
        } else if ("com.android.providers.downloads.documents" == uri.authority) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri: Uri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if ("com.android.providers.media.documents" == uri.authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
        return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}

private fun getDataColumn(
    context: Context, uri: Uri, selection: String?,
    selectionArgs: Array<String>?
): String? {
    val column = MediaStore.MediaColumns.DATA
    val projection = arrayOf(column)
    return context.contentResolver.query(
        uri, projection, selection, selectionArgs, null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val columnIndex: Int = cursor.getColumnIndex(column)
            return@use cursor.getString(columnIndex)
        }
        return@use null
//        return@use if (true) {
//            val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
//            cursor.getString(columnIndex)
//        } else null
    }
}

//val Uri.filePath: String?
//    get() = app.contentResolver.query(
//        this, arrayOf(MediaStore.MediaColumns.DATA), null, null, null
//    )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

private const val MEDIA_VIDEO_DIR = "ButterVideo"
private const val MEDIA_PICTURE_DIR = "ButterPicture"

inline val Uri.mediaStoreId: Long get() = ContentUris.parseId(this)

/**
 * Create image file by [MediaStore]
 *
 * @receiver should be a filename with extension, but without parent path
 */
fun String.createPictureFile() = createMediaFile(Environment.DIRECTORY_PICTURES, MEDIA_PICTURE_DIR)

/**
 * Create video file by [MediaStore]
 *
 * @receiver should be a filename with extension, but without parent path
 */
fun String.createVideoFile() = createMediaFile(Environment.DIRECTORY_MOVIES, MEDIA_VIDEO_DIR)

private fun String.createMediaFile(bucket: String, subDir: String): Uri {
    @Suppress("DEPRECATION") val parentPath =
        "${Environment.getExternalStoragePublicDirectory(bucket).absolutePath}/$subDir"
    val parentDir = File(parentPath)
    if (parentDir.exists().not()) parentDir.mkdirs()

    val file = File(parentDir, this)
    if (file.exists().not()) file.createNewFile()
    return file.toUri()
}

@Suppress("DEPRECATION")
fun Uri.notifyMediaScanner() {
    app.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
        data = this@notifyMediaScanner.toFileUri()
    })
}

internal val Uri.isFileScheme: Boolean get() = ContentResolver.SCHEME_FILE == scheme
internal val Uri.isContentScheme: Boolean get() = ContentResolver.SCHEME_CONTENT == scheme

fun Uri.toFileUri(): Uri? {
    return when {
        isContentScheme -> Uri.fromFile(File(getDataColumn() ?: return null))
        isFileScheme -> this
        else -> null
    }
}

fun Uri.delete(): Boolean = toFile().delete()

val Uri.filePath: String?
    get() {
        if (DocumentsContract.isDocumentUri(app, this)) {
            if ("com.android.externalstorage.documents" == authority) {
                val docId = DocumentsContract.getDocumentId(this)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    @Suppress("DEPRECATION")
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == authority) {
                val id = DocumentsContract.getDocumentId(this)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return contentUri.getDataColumn()
            } else if ("com.android.providers.media.documents" == authority) {
                val docId = DocumentsContract.getDocumentId(this)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return contentUri?.getDataColumn(selection, selectionArgs)
            }
        } else if ("content".equals(scheme, ignoreCase = true)) {
            return try {
                getDataColumn()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                // If the content uri is exposed by other apps and no data column found,
                // return the uri path directly.
                path
            }
        } else if ("file".equals(scheme, ignoreCase = true)) {
            return path
        }
        return null
    }

@Suppress("DEPRECATION")
private fun Uri.getDataColumn(
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String? {
    app.contentResolver.query(
        this,
        arrayOf(MediaStore.MediaColumns.DATA),
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            return cursor.getString(columnIndex)
        }
    }
    return null
}
