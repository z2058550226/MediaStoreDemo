package com.bybutter.mediatest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bybutter.mediatest.apitest.AppSpecificFileActivity
import com.bybutter.mediatest.apitest.MediaStoreActivity
import com.bybutter.mediatest.apitest.SafActivity
import timber.log.Timber
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.e("#3 main oncreate")
        Toast.makeText(this, "main activity", Toast.LENGTH_SHORT).show()
    }

    private fun start(kClass: KClass<out Activity>) = startActivity(Intent(this, kClass.java))

    fun butterTest(view: View) = start(BucketListActivity::class)
    fun accessAppSpecificFile(view: View) = start(AppSpecificFileActivity::class)
    fun mediaStore(view: View) = start(MediaStoreActivity::class)
    fun storageAccessFramework(view: View) = start(SafActivity::class)
    fun imageDisplay(view: View) = start(ImageDisplayActivity::class)
    fun pickPicture(view: View) = start(PickPictureActivity::class)
    fun simpleTest(view: View) = start(SimpleTestActivity::class)
    fun testCreateFile(view: View) = start(CreateFileActivity::class)
    fun bucketList2(view: View) = start(BucketListActivity2::class)
    fun imagePriview(view: View) = start(ImagePreviewActivity::class)
}