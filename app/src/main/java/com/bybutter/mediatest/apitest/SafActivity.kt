package com.bybutter.mediatest.apitest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.bybutter.mediatest.R
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Storage Access Framework
 *
 * SAF本地存储服务的围绕 DocumentsProvider实现的，通过Intent调用DocumentUI，
 * 由用户在DocumentUI上选择要创建、授权的文件以及目录等，
 * 授权成功后再onActivityResult回调用拿到指定的Uri，根据这个Uri可进行读写等操作，
 * 这时候已经赋予文件读写权限，不需要再动态申请权限
 */
class SafActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_FOR_SINGLE_FILE = 0x321
        const val REQUEST_CODE_FOR_DOCUMENT_DIR = 0x111
        const val WRITE_REQUEST_CODE = 0x2234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_access_framework)
//        searchSingleFile()
        createSingleFile()
    }

    private fun searchSingleFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            addCategory(Intent.CATEGORY_OPENABLE)

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            type = "image/*"
        }
        startActivityForResult(intent,
            REQUEST_CODE_FOR_SINGLE_FILE
        )
    }

    private fun createSingleFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.
            type = "image/*"
            putExtra(Intent.EXTRA_TITLE, "suika.png")
        }

        startActivityForResult(intent,
            WRITE_REQUEST_CODE
        )
    }

    private fun deleteFile(uri: Uri) {
        val deleted = DocumentsContract.deleteDocument(contentResolver, uri)
    }

    private fun editFile(uri: Uri) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                // use{} lets the document provider know you're done by automatically closing the stream
                FileOutputStream(it.fileDescriptor).use { os ->
                    os.write(
                        ("Overwritten by MyCloud at ${System.currentTimeMillis()}\n").toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getDir() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent,
            REQUEST_CODE_FOR_DOCUMENT_DIR
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("requestCode: $requestCode")
        Timber.e("resultCode: $resultCode")
        Timber.e("data: $data")

        // getDir result
        if (requestCode == REQUEST_CODE_FOR_DOCUMENT_DIR) {
            //选择目录
            if (resultCode == Activity.RESULT_OK) {
                val treeUri = data?.data
                if (treeUri != null) {
                    //implementation 'androidx.documentfile:documentfile:1.0.1'
                    val root = DocumentFile.fromTreeUri(this, treeUri)
                    root?.listFiles()?.forEach { it ->
                        Timber.e("目录下文件名称：${it.name}")
                    }
                }
            }

        }
    }
}