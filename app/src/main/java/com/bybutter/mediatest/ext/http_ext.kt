package com.bybutter.mediatest.ext

import okhttp3.OkHttpClient

val httpClient by lazy {
    OkHttpClient.Builder().build()
}