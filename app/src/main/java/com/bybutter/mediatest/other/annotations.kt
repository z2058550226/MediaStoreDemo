package com.bybutter.mediatest.other

/**
 * This annotation indicates the uri should be content:// scheme
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ContentUri

/**
 * This annotation indicates the uri should be file:// scheme
 *
 * If the path of this uri is not app-specific path, it is not a [StreamUri]
 */
@Retention(AnnotationRetention.SOURCE)
annotation class FileUri

/**
 * This annotation indicates the scheme of uri can be content:// or file:// . that
 * means the uri is used for opening an IO stream or getting a file descriptor.
 *
 * For file:// scheme uri, it must be an app-specific path
 */
@Retention(AnnotationRetention.SOURCE)
annotation class StreamUri