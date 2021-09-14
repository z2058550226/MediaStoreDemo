package com.bybutter.mediatest.widget

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bybutter.mediatest.ext.dp
import com.bybutter.mediatest.ext.dpf
import kotlinx.coroutines.*
import timber.log.Timber

class ImagePreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    private val cardView: CardView
    private val imageView: ImageView

    private val paddingWidth = 24f.dp
    private var showing = false
    private val animatorRect = Rect()
    private var currentJob: Job? = null

    init {
        addView(CardView(context).apply {
            setCardBackgroundColor(-1)
            elevation = 24f.dpf
            radius = 12f.dpf
            visibility = GONE
            cardView = this

            addView(ImageView(context).apply {
                imageView = this
                scaleType = ImageView.ScaleType.CENTER_CROP
            })
        })
    }

    fun show(originalView: View, uri: Uri) {
        currentJob = MainScope().launch {
            showing = true
            val bitmap: Bitmap = withContext(Dispatchers.IO) {
                Glide.with(context).asBitmap().load(uri).submit().get()
            }

            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            val viewWidth = width
            val viewHeight = height
            val cardMaxWidth = viewWidth - paddingWidth * 2
            val cardMaxHeight = viewHeight - paddingWidth * 2

            val bitmapAspectRatio = bitmapWidth * 1f / bitmapHeight

            val relyOnWidth = bitmapAspectRatio > cardMaxWidth * 1f / cardMaxHeight

            val cardWidth: Int
            val cardHeight: Int
            if (relyOnWidth) {
                cardWidth = cardMaxWidth
                cardHeight = (cardWidth / bitmapAspectRatio + 0.5f).toInt()
            } else {
                cardHeight = cardMaxHeight
                cardWidth = (cardHeight * bitmapAspectRatio + 0.5f).toInt()
            }

            imageView.setImageBitmap(bitmap)

            // show card view
            val startLeft = originalView.left
            val startTop = originalView.top
            val startRight = originalView.right
            val startBottom = originalView.bottom

            val widthBorder = (viewWidth - cardWidth) / 2
            val heightBorder = (viewHeight - cardHeight) / 2
            val endLeft = left + widthBorder
            val endTop = top + heightBorder
            val endRight = right - widthBorder
            val endBottom = bottom - heightBorder
            val leftAnimator = ValueAnimator.ofInt(startLeft, endLeft)
            leftAnimator.addUpdateListener {
                animatorRect.left = it.animatedValue as Int
            }
            val topAnimator = ValueAnimator.ofInt(startTop, endTop)
            topAnimator.addUpdateListener {
                animatorRect.top = it.animatedValue as Int
            }
            val rightAnimator = ValueAnimator.ofInt(startRight, endRight)
            rightAnimator.addUpdateListener {
                animatorRect.right = it.animatedValue as Int
            }
            val bottomAnimator = ValueAnimator.ofInt(startBottom, endBottom)
            bottomAnimator.addUpdateListener {
                animatorRect.bottom = it.animatedValue as Int
                imageView.requestLayout()
                Timber.e("${imageView.left} ${imageView.top} ${imageView.right} ${imageView.bottom}")
            }

            val rectAnimation = AnimatorSet()
            rectAnimation.playTogether(leftAnimator, topAnimator, rightAnimator, bottomAnimator)
            rectAnimation.duration = 500L

            rectAnimation.start()
            cardView.visibility = VISIBLE
        }
    }

    fun hide() {
        currentJob?.cancel()
        cardView.visibility = GONE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        cardView.layout(
            animatorRect.left,
            animatorRect.top,
            animatorRect.right,
            animatorRect.bottom
        )
        imageView.layout(
            0,
            0,
            animatorRect.width(),
            animatorRect.height()
        )
    }
}