package com.bybutter.mediatest

import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.contentValuesOf
import androidx.core.graphics.toColorInt
import com.bybutter.mediatest.ext.httpClient
import com.bybutter.mediatest.ext.insertPictureToMediaStore
import com.bybutter.mediatest.ext.load
import kotlinx.android.synthetic.main.activity_simple_test.*
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection
import kotlin.concurrent.thread

class SimpleTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_test)
//        testOnlyUri()
//        testUseFile()
//        testSaveOnlineFile()
        test3()
    }

    private fun testSaveOnlineFile() {
        val url = "https://m0-img.bybutter.com/3E611A4C-CEAF-4A76-9BB3-FE50A720C8B2.jpg"

        thread {
            val request = Request.Builder()
                .url(url)
//                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body!!
            val contentLength = body.contentLength()
            var resultUri: Uri? = null
            body.byteStream().use { inputStream ->
                resultUri = "suika_${System.currentTimeMillis()}.jpg".insertPictureToMediaStore()!!
                contentResolver.openOutputStream(resultUri!!)!!.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Timber.e("download finished")
            runOnUiThread {
                Toast.makeText(app, "haha", Toast.LENGTH_SHORT).show()
                gv.load(resultUri ?: return@runOnUiThread)
            }

        }
    }

    private fun testUseFile() {
        val parentPath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}/suikaTest"
        val parentDir = File(parentPath)
        if (parentDir.exists().not()) parentDir.mkdirs()

        val filename = "suika1.jpg"
        val filepath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}/suikaTest/$filename"
        val file = File(filepath)
        val insertUri: Uri?
        if (file.exists().not()) {
            file.createNewFile()
            insertUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValuesOf(
                    MediaStore.MediaColumns.DATA to filepath,
                    MediaStore.MediaColumns.DISPLAY_NAME to filename,
                    MediaStore.MediaColumns.MIME_TYPE to URLConnection.guessContentTypeFromName(
                        filename
                    ),
//                MediaStore.MediaColumns.RELATIVE_PATH to "${Environment.DIRECTORY_DCIM}/suika"
                    MediaStore.MediaColumns.DATE_MODIFIED to System.currentTimeMillis() / 1000
                )
            )
        } else {
            Timber.e("file already exist.")
            val id = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(BaseColumns._ID),
                "${MediaStore.MediaColumns.DATA}=?",
                arrayOf(filepath),
                null
            )?.use {
                if (it.moveToFirst()) {
                    return@use it.getLong(0)
                }
                return@use null
            }
            insertUri = if (id != null) {
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            } else {
                null
            }
        }


        Timber.e("insertUri: $insertUri")
        if (insertUri == null) {
            Timber.e("insert fail")
            return
        }

        contentResolver.openOutputStream(insertUri)?.use { os ->
            try {
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                //创建了一个红色的图片
                val canvas = Canvas(bitmap)
                canvas.drawColor("#FFFF66CC".toColorInt())
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, os)
                Timber.e("创建Bitmap成功")
            } catch (e: IOException) {
                Timber.e("创建失败：${e.message}")
            }
            os.flush()
        }

        contentResolver.query(
            insertUri,
            arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE
            ),
            null, null, null
        )?.use {
            it.moveToFirst()
            val data = it.getString(0)
            val name = it.getString(1)
            val mimeType = it.getString(2)
            Timber.e("data: $data")
            Timber.e("name: $name")
            Timber.e("mimeType: $mimeType")
        }

        gv.load(insertUri)
    }

    private fun testOnlyUri() {
        val resolver = contentResolver
        val values = ContentValues()
        val filename = "Image4.png"
//        val filepath =
//            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath}/suikaTest/$filename"
//        values.put(MediaStore.MediaColumns.DATA, filepath)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image.png")
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an image")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.TITLE, "Image.png")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/suika_test/")

        val insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (insertUri == null) {
            Timber.e("insert uri is null, failed")
            return
        }

        resolver.openOutputStream(insertUri)?.use { os ->
            try {
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                //创建了一个红色的图片
                val canvas = Canvas(bitmap)
                canvas.drawColor(0xFF66FFCC.toInt())
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, os)
                Timber.e("创建Bitmap成功")
            } catch (e: IOException) {
                Timber.e("创建失败：${e.message}")
            }
            os.flush()
        }

        gv.load(insertUri)
    }

    private fun test2() {
        val filename = "suika.jpg"
        val externalStorageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val file = File(externalStorageDirectory, filename)
        if (file.exists().not()) file.createNewFile()

        FileOutputStream(file).use {
            FileOutputStream(file).use { os ->
                try {
                    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                    //创建了一个红色的图片
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(0xFF66FFCC.toInt())
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, os)
                    Timber.e("创建Bitmap成功")
                } catch (e: IOException) {
                    Timber.e("创建失败：${e.message}")
                }
                os.flush()
            }
        }

        val url = MediaStore.Images.Media.insertImage(
            contentResolver,
            file.absolutePath,
            filename,
            "some pic"
        )
        Timber.e(url)
        gv.load(url)
    }

    private val testUri = "content://media/external/file/17046"
    private fun test3() {
        iv.load(testUri)
    }
}