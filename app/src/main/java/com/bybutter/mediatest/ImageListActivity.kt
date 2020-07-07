package com.bybutter.mediatest

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bybutter.mediatest.base.ListActivity
import com.bybutter.mediatest.bean.Image
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.activity_image_list.*

class ImageListActivity : ListActivity<Image>() {
    companion object {
        private const val K_BUCKET_ID = "k_bucket_id"
        fun start(activity: Activity, bucketId: Long) {
            activity.startActivity(Intent(activity, ImageListActivity::class.java).apply {
                putExtra(K_BUCKET_ID, bucketId)
            })
        }
    }

    private val bucketId by lazy { intent.getLongExtra(K_BUCKET_ID, 0L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)
        queryImage()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = Adapter()
    }

    private fun queryImage() {
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME
            ),
            "${MediaStore.MediaColumns.BUCKET_ID}=?", arrayOf(bucketId.toString()), "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            dataList.clear()
            while (cursor.moveToNext()) {
                val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
                val displayNameColumnIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val id = cursor.getLong(idColumnIndex)
                dataList += Image(
                    id, cursor.getString(displayNameColumnIndex),
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                )
            }
        }
    }

    override val itemLayout = R.layout.item

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tvItem: TextView = holder.itemView.findViewById(R.id.tv_item)
        val ivItem: SimpleDraweeView = holder.itemView.findViewById(R.id.iv_item)
        val image = dataList[position]
        tvItem.text = image.displayName
        ivItem.setImageURI(image.uri)
    }
}