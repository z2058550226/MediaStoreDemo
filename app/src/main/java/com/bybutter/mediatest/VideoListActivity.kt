package com.bybutter.mediatest

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Size
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bybutter.mediatest.base.ListActivity
import com.bybutter.mediatest.bean.Video
import com.bybutter.mediatest.ext.load
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.datasource.SimpleDataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.CloseableBitmap
import kotlinx.android.synthetic.main.activity_list.*
import timber.log.Timber

class VideoListActivity : ListActivity<Video>() {
    companion object {
        private const val K_BUCKET_ID = "k_bucket_id"
        fun start(activity: Activity, bucketId: Long) {
            activity.startActivity(Intent(activity, VideoListActivity::class.java).apply {
                putExtra(K_BUCKET_ID, bucketId)
            })
        }
    }

    private val bucketId by lazy { intent.getLongExtra(K_BUCKET_ID, 0L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        queryVideo()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = Adapter()
    }

    private fun queryVideo() {
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME
            ),
            "${MediaStore.MediaColumns.BUCKET_ID}=?",
            arrayOf(bucketId.toString()),
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            dataList.clear()
            while (cursor.moveToNext()) {
                val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
                val displayNameColumnIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)

                val id = cursor.getLong(idColumnIndex)
                val displayName = cursor.getString(displayNameColumnIndex)

                dataList += Video(
                    id, displayName,
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                )
            }
        }
    }

    override val itemLayout = R.layout.item

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tvItem: TextView = holder.itemView.findViewById(R.id.tv_item)
        val ivItem: SimpleDraweeView = holder.itemView.findViewById(R.id.iv_item)
        val video = dataList[position]
        tvItem.text = video.displayName

        val thumbnailBitmap = contentResolver.loadThumbnail(video.uri, Size(480, 480), null)
        Timber.e("new bitmap: ${video.uri}")
//        val cr = CloseableReference.of(thumbnailBitmap) { it.recycle() }
//        val controller = Fresco.newDraweeControllerBuilder().apply {
//            oldController = ivItem.controller
//            setDataSourceSupplier {
//                SimpleDataSource.create<CloseableReference<CloseableBitmap>>()
//            }

//        }.build()

//        ivItem.setImageURI(loadThumbnail)


//        ivItem.load(video.uri)
        ivItem.setImageBitmap(thumbnailBitmap)
    }
}