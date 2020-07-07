package com.bybutter.mediatest

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bybutter.mediatest.ext.load
import kotlinx.android.synthetic.main.activity_image_display.*

class ImageDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)
//        iv_display.load(Uri.parse("res:///${R.drawable.album_camera_button}"))
//        iv_display.setImageURI(Uri.parse("res:///${R.drawable.album_camera_button}"))
//        iv_display.setImageResource(R.drawable.album_camera_button)
//        iv_display.load(R.drawable.album_camera_button)
//        iv_display.load("res:///${R.drawable.album_camera_button}")
        val uri = Uri.parse("res:///${R.drawable.album_camera_button}")
//        iv_display.setImageURI(uri)
        iv_display.load(uri)
    }
}