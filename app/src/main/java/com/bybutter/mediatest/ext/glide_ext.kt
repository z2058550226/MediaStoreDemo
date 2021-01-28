package com.bybutter.mediatest.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bybutter.mediatest.R

fun ImageView.load(uri: Any) =
    Glide.with(this).load(uri).placeholder(R.color.colorImagePlaceHolder).into(this)