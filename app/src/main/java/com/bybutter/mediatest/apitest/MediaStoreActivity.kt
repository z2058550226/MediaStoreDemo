package com.bybutter.mediatest.apitest

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bybutter.mediatest.R
import com.bybutter.mediatest.app
import kotlinx.android.synthetic.main.activity_media_store.*
import timber.log.Timber
import java.io.IOException

class MediaStoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_store)
//        outputAllVolume()
//        createFileByMediaStore()
//        val queryUri = queryFileByMediaStore("Image.png")
//        queryUri?.let { readFileByMediaStore(it) }
//        queryUri?.let { loadThumbnailByMediaStore(it) }

        modifyFileByMediaStore()
    }

    private val someVolumeName = "somevolumename"

    private val audioUri = mapOf(
        MediaStore.Audio.Media.INTERNAL_CONTENT_URI to "content://media/internal/audio/media",
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to "content://media/external/audio/media",
        MediaStore.Audio.Media.getContentUri(someVolumeName) to "content://media/$someVolumeName/audio/media"
    )
    private val videoUri = mapOf(
        MediaStore.Video.Media.INTERNAL_CONTENT_URI to "content://media/internal/video/media",
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI to "content://media/external/video/media",
        MediaStore.Video.Media.getContentUri(someVolumeName) to "content://media/$someVolumeName/video/media"
    )
    private val imageUri = mapOf(
        MediaStore.Images.Media.INTERNAL_CONTENT_URI to "content://media/internal/image/media",
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI to "content://media/external/image/media",
        MediaStore.Images.Media.getContentUri(someVolumeName) to "content://media/$someVolumeName/image/media"
    )
    private val downloadUri = mapOf(
        MediaStore.Downloads.INTERNAL_CONTENT_URI to "content://media/internal/downloads",
        MediaStore.Downloads.EXTERNAL_CONTENT_URI to "content://media/external/downloads",
        MediaStore.Downloads.getContentUri(someVolumeName) to "content://media/$someVolumeName/download"
    )
    private val fileUri = mapOf(
        MediaStore.Files.getContentUri(someVolumeName) to "content://media/$someVolumeName/file"
    )

    // 输出所有存储卷，包括手机上插的u盘（或者mnt之类?），一般就外部存储一个卷
    private fun outputAllVolume() {
        MediaStore.getExternalVolumeNames(app).forEach { volumeName ->
            Timber.e(
                "volumeName: $volumeName, uri：${MediaStore.Images.Media.getContentUri(volumeName)}"
            )
        }
    }

    private fun createFileByMediaStore() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image.png")
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an image")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.TITLE, "Image.png")
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/sl")
        val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val insertUri = contentResolver.insert(external, values) ?: return

        contentResolver.openOutputStream(insertUri).use { os ->
            try {
                val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                //创建了一个红色的图片
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.RED)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, os)
                Timber.e("创建Bitmap成功")
            } catch (e: IOException) {
                Timber.e("创建失败：${e.message}")
            }
        }
    }

    private fun queryFileByMediaStore(displayName: String): Uri? {
        val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Images.Media.DISPLAY_NAME}=?"
        val args = arrayOf(displayName)
        val projection = arrayOf(MediaStore.Images.Media._ID)
        contentResolver.query(external, projection, selection, args, null).use { cursor ->
            cursor ?: return@use
            cursor.moveToFirst()
            val queryUri = ContentUris.withAppendedId(external, cursor.getLong(0))
            Timber.e("查询成功，Uri路径$queryUri")
            return queryUri
        }
        return null
    }

    private fun readFileByMediaStore(queryUri: Uri) {
        try {
            contentResolver.openFileDescriptor(queryUri, "r").use { pfd ->
                pfd ?: return@use
                val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                iv.setImageBitmap(bitmap)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadThumbnailByMediaStore(queryUri: Uri) {
        iv.setImageBitmap(contentResolver.loadThumbnail(queryUri, Size(640, 480), null))
    }

    private fun nativeAccessFile(uri: Uri) {
        val fileOpenMode = "r"
        val parcelFd =
            contentResolver.openFileDescriptor(uri, fileOpenMode)
        if (parcelFd != null) {
            val fd = parcelFd.detachFd();
            // Pass the integer value "fd" into your native code. Remember to call
            // close(2) on the file descriptor when you're done using it.
        }
    }

    private val SENDER_REQUEST_CODE = 0x123
    private var tmpRequestUri: Uri? = null

    // 修改其他app创建的多媒体文件
    private fun modifyFileByMediaStore() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //这里的img 是我相册里的，如果运行demo，可以换成你自己的
            val queryUri =
                queryFileByMediaStore("me.jpg") ?: return
            try {
                contentResolver.openOutputStream(queryUri).use {
                    val bm = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bm)
                    canvas.drawColor(0xFF66CCFF.toInt())
                    bm.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e1: RecoverableSecurityException) {
                e1.printStackTrace()
                //捕获 RecoverableSecurityException异常，发起请求

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        tmpRequestUri = queryUri
                        startIntentSenderForResult(
                            e1.userAction.actionIntent.intentSender,
                            SENDER_REQUEST_CODE,
                            Intent().apply { data = queryUri },
                            0,
                            0,
                            0
                        )
                    }
                } catch (e2: IntentSender.SendIntentException) {
                    e2.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tmpRequestUri?.let { uri ->
            if (requestCode == SENDER_REQUEST_CODE) {
                contentResolver.openOutputStream(uri).use {
                    val bm = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bm)
                    canvas.drawColor(0xFF66CCFF.toInt())
                    bm.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                tmpRequestUri = null
            }
        }

        Timber.e("requestCode: $requestCode")
        Timber.e("data: $data")
    }

    private fun deleteFileByMediaStore(uri: Uri) {
        contentResolver.delete(uri, null, null)
    }

    // #符号表示Media.xx.Media._ID，Long类型
    private val audioStorage =
        StorageMapping(
            "音频", "audio/*",
            listOf("image/media", "image/media/#"),
            listOf(
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES
            )
        )

    private val albumArtsStorage =
        StorageMapping(
            "音乐专辑艺术家", "image/*",
            listOf("audio/albumart", "audio/albumart/#"),
            listOf(Environment.DIRECTORY_MUSIC)
        )

    private val musicListStorage =
        StorageMapping(
            "音乐播放列表", "NA",
            listOf("audio/playlists", "audio/playlists/#"),
            listOf(Environment.DIRECTORY_MUSIC)
        )

    private val videoStorage =
        StorageMapping(
            "视频", "video/*",
            listOf("video/media", "video/media/#"),
            listOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_MOVIES)
        )

    private val imageStorage =
        StorageMapping(
            "图片", "image/*",
            listOf("images/media", "images/media/#"),
            listOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_PICTURES)
        )

    private val videoThumbnailStorage =
        StorageMapping(
            "视频缩略图", "image/*",
            listOf("video/thumbnails", "video/thumbnails/#"),
            listOf(Environment.DIRECTORY_MOVIES)
        )

    private val imageThumbnailStorage =
        StorageMapping(
            "图片缩略图", "image/*",
            listOf("image/thumbnails", "image/thumbnails/#"),
            listOf(Environment.DIRECTORY_PICTURES)
        )

    // 上面的可以在授予READ_EXTERNAL_STORAGE权限的情况下使用，下面这俩不行，只能用自己app的
    private val downloadDataStorage =
        StorageMapping(
            "下载数据", "NA",
            listOf("downloads", "downloads/#"),
            listOf(Environment.DIRECTORY_DOWNLOADS)
        )

    private val fileStorage =
        StorageMapping(
            "其他文件", "NA",
            listOf("file", "file/#"),
            listOf(Environment.DIRECTORY_DOWNLOADS, Environment.DIRECTORY_DOCUMENTS)
        )

    data class StorageMapping(
        val dataType: String,
        val mimeType: String,
        val uriPath: List<String>,
        val optionalDirectory: List<String>
    )
}