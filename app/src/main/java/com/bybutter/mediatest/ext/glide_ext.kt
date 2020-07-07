package com.bybutter.mediatest.ext

import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.load(uri: Any) = Glide.with(this).load(uri).into(this)