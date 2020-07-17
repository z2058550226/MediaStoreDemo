package com.bybutter.mediatest

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.bybutter.mediatest.ext.filePath
import kotlinx.android.synthetic.main.activity_image_display.*
import timber.log.Timber

class ImageDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)
//        val uri = Uri.parse("res:///${R.drawable.album_camera_button}")
//        iv_display.load(uri)

        val uri = Uri.parse("content://media/external/images/media/35107")

        contentResolver.query(
            uri,
            arrayOf(BaseColumns._ID, MediaStore.MediaColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            cursor.moveToFirst()
            val size = cursor.getLong(1)
            Timber.e("size: $size")
        }
//        contentResolver.query(
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//            arrayOf(BaseColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
//            null, null, null
//        )?.use { cursor ->
//            if (cursor.moveToFirst()) {
//                val mediaId = cursor.getLong(0)
//                val mediaUri = ContentUris.withAppendedId(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId
//                )
//                mediaUri.filePath?.let { imageFilePath ->
//                    Timber.e("imageFilePath: $imageFilePath")
//                    val bitmap = BitmapFactory.decodeFile(imageFilePath)
//                    iv_display.setImageBitmap(bitmap)
//                }
//            }
//        }
    }
}