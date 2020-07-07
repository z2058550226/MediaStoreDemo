package com.bybutter.mediatest.apitest

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.bybutter.mediatest.R
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * App-Specific Directory 这里简称沙盒目录，会随app卸载而删除
 */
class AppSpecificFileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_specific_file)
        val documents = getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)
        if (documents.isNotEmpty()) {
            val dir = documents[0]

            try {
                val newFile = File(dir.absolutePath, "MyDocument")
                FileOutputStream(newFile).use { os ->
                    os.write("create a file".toByteArray(Charsets.UTF_8))
                    os.flush()
                    Timber.e("创建成功")
                    dir.listFiles()?.forEach { file: File? ->
                        if (file != null) {
                            Timber.e("Documents 目录下的文件名：${file.name}")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Timber.e("创建失败")
            }
        }
    }

    /**
     * 下面这三个是外部存储中的沙盒目录
     */
    // obb主要是游戏app用到
    private val myExternalObbDir by lazy { obbDir }

    // sdcard/android/data/com.bybutter.mediatest/files/MyFileDir
    private val myExternalFileDir by lazy { getExternalFilesDir("MyFileDir") }

    // sdcard/android/data/com.bybutter.mediatest/cache
    private val myExternalCacheDir by lazy { externalCacheDir }

    /**
     * 下面这四个是所有volume中的沙盒目录
     */
    private val allMediaDirs by lazy { externalMediaDirs }
    private val allObbDirs by lazy { obbDirs }
    private val allCacheDirs by lazy { externalCacheDirs }
    private val allFileDirs by lazy { getExternalFilesDirs("MyFileDir") }
}