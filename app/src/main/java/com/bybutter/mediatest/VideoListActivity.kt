package com.bybutter.mediatest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bybutter.mediatest.base.ListActivity
import com.bybutter.mediatest.bean.Video
import com.bybutter.mediatest.ext.insertVideoToMediaStore
import com.bybutter.mediatest.ext.saveToUri
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
    private val rv: RecyclerView by lazy { findViewById(R.id.rv) }

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
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_MODIFIED
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
                val sizeColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val dateAddedColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val dateModifiedColumnIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)

                val id = cursor.getLong(idColumnIndex)
                val displayName = cursor.getString(displayNameColumnIndex)
                val size = cursor.getLong(sizeColumnIndex)
                val dateAdded = cursor.getLong(dateAddedColumnIndex)
                val dateModified = cursor.getLong(dateModifiedColumnIndex)

                dataList += Video(
                    id,
                    displayName,
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                    size,
                    dateAdded,
                    dateModified
                )
            }
        }
    }

    override val itemLayout = R.layout.item

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int) {
        val tvItem: TextView = holder.itemView.findViewById(R.id.tv_item)
        val ivItem: ImageView = holder.itemView.findViewById(R.id.iv_item)
        val video = dataList[position]
        tvItem.text = "${video.displayName}\n${video.size}"

//        val thumbnailBitmap = contentResolver.loadThumbnail(video.uri, Size(480, 480), null)
//        Timber.e("new bitmap: ${video.uri}")
//        ivItem.setImageBitmap(thumbnailBitmap)

        ivItem.setImageURI(video.uri)
//        ivItem.load(video.uri)

        holder.itemView.setOnClickListener {
            val uri = "suika_${System.currentTimeMillis()}.mp4".insertVideoToMediaStore()
            val currentUri =
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video.id)
            if (uri == null) {
                Timber.e("insert fail")
                return@setOnClickListener
            }
            currentUri.saveToUri(uri)
            Toast.makeText(app, "save finished", Toast.LENGTH_SHORT).show()
        }
    }
}