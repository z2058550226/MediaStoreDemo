package com.bybutter.mediatest.ext

import com.bybutter.mediatest.app

val Float.dp: Int get() = (this * app.resources.displayMetrics.density + 0.5f).toInt()
val Float.dpf: Float get() = this * app.resources.displayMetrics.density
