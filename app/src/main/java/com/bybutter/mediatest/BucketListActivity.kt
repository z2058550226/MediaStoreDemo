package com.bybutter.mediatest

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bybutter.mediatest.base.ListActivity
import com.bybutter.mediatest.bean.Bucket
import com.bybutter.mediatest.ext.load
import kotlinx.android.synthetic.main.activity_bucket_list.*
import timber.log.Timber

@SuppressLint("SetTextI18n")
class BucketListActivity : ListActivity<Bucket>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bucket_list)
        queryFolder()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = Adapter()
    }

    private val a get() = ""
    val b :String by ::a

    private fun queryFolder() {
        val bucketIdSet = mutableSetOf<String?>()

        fun queryBucket(uri: Uri) {
            contentResolver.query(
                uri,
                arrayOf(
                    BaseColumns._ID,
                    MediaStore.MediaColumns.BUCKET_ID,
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                ),
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?",
                arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                ),
//                null,null,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val bucketIdColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID)
                    val bucketId = cursor.getString(bucketIdColumn)

                    if (bucketId in bucketIdSet) continue
                    bucketIdSet += bucketId

                    val idColumn = cursor.getColumnIndex(BaseColumns._ID)
                    val bucketDisplayNameColumn =
                        cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                    val mediaTypeColumn =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)

                    val id = cursor.getLong(idColumn)
                    val bucketDisplayName = cursor.getString(bucketDisplayNameColumn)
                    val mediaType = cursor.getInt(mediaTypeColumn)
                    val bucketUri = when (mediaType) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                            )
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                            ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                            )
                        else -> ContentUris.withAppendedId(uri, id)
                    }

                    val mimeType = contentResolver.getType(bucketUri)

                    dataList += Bucket(
                        bucketId.toLong(), bucketDisplayName,
                        bucketUri, mimeType, mediaType
                    )
                }
            }
        }

        dataList.clear()
        queryBucket(MediaStore.Files.getContentUri("external"))
//        queryBucket(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    override val itemLayout = R.layout.item

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tvItem: TextView = holder.itemView.findViewById(R.id.tv_item)
        val ivItem: ImageView = holder.itemView.findViewById(R.id.iv_item)

        val bucket = dataList[position]

        tvItem.text = "name: ${bucket.displayName}\nmimeType: ${bucket.mimeType}"

//        val imageDecodeOptions: ImageDecodeOptions =
//            ImageDecodeOptions.newBuilder().setForceStaticImage(true).build()
//
//        val request =
//            ImageRequestBuilder.newBuilderWithSource(bucket.bucketUri)
//                .setImageDecodeOptions(imageDecodeOptions)
//                .setResizeOptions(ResizeOptions(480, 480))
//                .build()
//
//        val controller = Fresco.newDraweeControllerBuilder()
//            .setOldController(ivItem.controller)
//            .setImageRequest(request)
//            .build() as PipelineDraweeController
//
//        ivItem.controller = controller

        ivItem.load(bucket.bucketUri)
        Timber.e("bucket.thumbnailUri: ${bucket.bucketUri}")
        Timber.e("bucket.bucketUri.authority: ${bucket.bucketUri.authority}")
//        Timber.e(
//            "getFullPathFromContentUri(this, bucket.bucketUri): ${getFullPathFromContentUri(
//                this,
//                bucket.bucketUri
//            )}"
//        )
        holder.itemView.setOnClickListener {
            when (bucket.mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                    ImageListActivity.start(this, bucket.bucketId!!)
                }
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                    VideoListActivity.start(this, bucket.bucketId!!)
                }
            }
        }
    }
}