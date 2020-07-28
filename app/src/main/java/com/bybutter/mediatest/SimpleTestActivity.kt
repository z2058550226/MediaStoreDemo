package com.bybutter.mediatest

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import com.bybutter.mediatest.base.ListActivity
import com.bybutter.mediatest.widget.GlideAvatar
import kotlinx.android.synthetic.main.activity_list.*
import timber.log.Timber

class SimpleTestActivity : ListActivity<String>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        repeat(100) {
            dataList += "item: $it"
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = Adapter()
    }

    override val itemLayout = R.layout.item_circle_image

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv_item).text = dataList[position]
        val gv = holder.itemView.findViewById<GlideAvatar>(R.id.ga_item)
        gv.doOnAttach {
            Timber.e("$position it: $it on attach")
        }

        gv.post {
            gv.doOnDetach {
                Timber.e("$position it: $it on detach")
            }
        }

        gv.doOnPreDraw {
            Timber.e("$position it: $it on pre draw")
        }
    }
}