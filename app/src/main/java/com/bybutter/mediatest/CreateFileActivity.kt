package com.bybutter.mediatest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.net.toFile
import com.bybutter.mediatest.ext.createPictureFile
import com.bybutter.mediatest.ext.notifyMediaScanner
import timber.log.Timber
import java.io.File

class CreateFileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_file)

        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        //创建了一个红色的图片
        val canvas = Canvas(bitmap)
        canvas.drawColor("#FFFF66CC".toColorInt())

        val oriFile = File(externalCacheDir, "test_launcher.png")
        oriFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
        }

        "c_suika.png".createPictureFile().also {
            Timber.e("created uri: $it")

            val targetFile = it.toFile()
            oriFile.copyTo(targetFile,true)
        }.notifyMediaScanner()

    }

}