package com.bybutter.mediatest

import android.annotation.SuppressLint
import android.content.ContentUris
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bybutter.mediatest.bean.Bucket

@SuppressLint("SetTextI18n")
class BucketListActivity2 : AppCompatActivity() {
    private val rv: RecyclerView by lazy {
        RecyclerView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(this@BucketListActivity2)
        }
    }

    private val bucketList = mutableListOf<Bucket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rv)

        val queryUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val adapter = Adapter()
        contentResolver.query(
            queryUri,
            arrayOf(
                BaseColumns._ID,
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            ),
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=? OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?",
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            ),
            MediaStore.MediaColumns.DATE_ADDED + " DESC"
        )?.use { cursor ->
            val buckets: MutableList<Bucket> = mutableListOf()

            while (cursor.moveToNext()) {
                val bucketId: Long = cursor.getLong(1)
                val filePath = cursor.getString(3)
                val findBucket = buckets.firstOrNull { it.bucketId == bucketId }
                if (findBucket != null) {
                    findBucket.itemCount++
                    continue
                }

                val contentId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
                val mediaType = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE))
                val thumbnailUri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentId
                        )
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentId
                        )
                    else -> ContentUris.withAppendedId(queryUri, contentId)
                }
                buckets += Bucket(
                    bucketId,
                    cursor.getString(2),
                    thumbnailUri,
                    null,
                    0,
                    1,
                    cursor.getLong(0)
                )
            }

            bucketList.clear()
            bucketList.addAll(buckets)
        }

        rv.adapter = adapter
    }

    private inner class Adapter : RecyclerView.Adapter<BucketViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BucketViewHolder {
            return BucketViewHolder(
                LayoutInflater.from(this@BucketListActivity2)
                    .inflate(R.layout.item_bucket, parent, false)
            )
        }

        override fun onBindViewHolder(holder: BucketViewHolder, position: Int) {
            val bucket = bucketList[position]
            Glide.with(holder.iv).load(bucket.bucketUri).into(holder.iv)
            holder.tv.text = "${bucket.displayName} \n ${bucket.mimeType}"
        }

        override fun getItemCount(): Int = bucketList.size
    }

    class BucketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.iv)
        val tv: TextView = view.findViewById(R.id.tv)
    }

}