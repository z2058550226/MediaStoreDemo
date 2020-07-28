package com.bybutter.mediatest

import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class ImageDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID,
                MediaStore.MediaColumns.SIZE,
                MediaStore.Images.Media.DATE_ADDED
            ),
            null,
            null,
            null
        )?.use { cursor ->

            cursor.moveToFirst()
            val size = cursor.getLong(1)
            Timber.e("size: $size")
        }
    }
}