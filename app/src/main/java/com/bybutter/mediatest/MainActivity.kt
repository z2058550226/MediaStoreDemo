package com.bybutter.mediatest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
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
    }

    private fun start(kClass: KClass<out Activity>) = startActivity(Intent(this, kClass.java))

    fun butterTest(view: View) = start(BucketListActivity::class)
    fun accessAppSpecificFile(view: View) = start(AppSpecificFileActivity::class)
    fun mediaStore(view: View) = start(MediaStoreActivity::class)
    fun storageAccessFramework(view: View) = start(SafActivity::class)
    fun imageDisplay(view: View) = start(ImageDisplayActivity::class)
    fun pickPicture(view: View) = start(PickPictureActivity::class)
}