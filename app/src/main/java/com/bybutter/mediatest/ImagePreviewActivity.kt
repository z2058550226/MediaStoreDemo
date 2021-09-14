package com.bybutter.mediatest

import android.annotation.SuppressLint
import android.content.ContentUris
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bybutter.mediatest.bean.Image
import com.bybutter.mediatest.ext.load
import com.bybutter.mediatest.widget.ImagePreviewView

class ImagePreviewActivity : AppCompatActivity() {
    private val dataList = mutableListOf<Image>()
    private val previewContainer: ImagePreviewView by lazy { findViewById(R.id.preview_container) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recycler_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        queryImage()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = Adapter()
    }

    private fun queryImage() {
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                BaseColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE
            ),
            null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            dataList.clear()
            while (cursor.moveToNext()) {
                val idColumnIndex = cursor.getColumnIndex(BaseColumns._ID)
                val displayNameColumnIndex =
                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val mimeTypeColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

                val id = cursor.getLong(idColumnIndex)
                val size = cursor.getLong(sizeColumnIndex)
                val mimeType = cursor.getString(mimeTypeColumnIndex)

                dataList += Image(
                    id, cursor.getString(displayNameColumnIndex),
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                    size,
                    mimeType
                )
            }
        }
    }

    inner class Adapter : RecyclerView.Adapter<IpViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IpViewHolder {
            val inflater = LayoutInflater.from(this@ImagePreviewActivity)
            val view = inflater.inflate(R.layout.item, parent, false)
            return IpViewHolder(view)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: IpViewHolder, position: Int) {
            val image = dataList[position]
            holder.tvItem.text = "${image.id} ${image.displayName}\n${image.size}"
            holder.ivItem.load(image.uri)
            holder.itemView.setOnLongClickListener {
                previewContainer.show(it, image.uri)
                true
            }
            holder.itemView.setOnTouchListener { v, event ->
                v.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    previewContainer.hide()
                }
                true
            }
        }

        override fun getItemCount() = dataList.size
    }

    class IpViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvItem: TextView = v.findViewById(R.id.tv_item)
        val ivItem: ImageView = v.findViewById(R.id.iv_item)
    }
}