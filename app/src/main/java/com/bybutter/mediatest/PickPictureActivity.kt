package com.bybutter.mediatest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream


class PickPictureActivity : AppCompatActivity() {
    companion object {
        const val INTENT_SELECT_PHOTO = 0x123
    }

    private val btn_pick: Button by lazy { findViewById(R.id.btn_pick) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_picture)
        btn_pick.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, INTENT_SELECT_PHOTO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_SELECT_PHOTO) {
            val uri: Uri = data?.data ?: return
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
        }
    }
}