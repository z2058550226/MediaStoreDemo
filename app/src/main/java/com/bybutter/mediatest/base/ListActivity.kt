package com.bybutter.mediatest.base

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

abstract class ListActivity<T> : AppCompatActivity() {
    abstract val itemLayout: Int
    protected open fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(layoutInflater.inflate(itemLayout, parent, false))

    abstract fun onBindViewHolder(holder: VH, position: Int)
    protected val dataList = mutableListOf<T>()

    inner class Adapter : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            this@ListActivity.onCreateViewHolder(parent, viewType)

        override fun getItemCount() = dataList.count()

        override fun onBindViewHolder(holder: VH, position: Int) =
            this@ListActivity.onBindViewHolder(holder, position)
    }

    class VH(v: View) : RecyclerView.ViewHolder(v)
}