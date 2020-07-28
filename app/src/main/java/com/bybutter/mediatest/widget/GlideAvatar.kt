package com.bybutter.mediatest.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import timber.log.Timber

class GlideAvatar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private val pathRT = Path()
    private val pathLT = Path()
    private val pathLB = Path()
    private val pathRB = Path()
    private val bound = RectF()

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(pathLB, paint)
        canvas.drawPath(pathLT, paint)
        canvas.drawPath(pathRB, paint)
        canvas.drawPath(pathRT, paint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Timber.e("onLayout")
        bound.set(0F, 0F, width.toFloat(), height.toFloat())
        pathRB.apply {
            reset()
            moveTo(bound.right, bound.bottom)
            lineTo(bound.right, bound.centerY())
            arcTo(bound, 0F, 90F)
            close()
        }
        pathLB.apply {
            reset()
            moveTo(0F, bound.bottom)
            lineTo(bound.centerX(), bound.bottom)
            arcTo(bound, 90F, 90F)
            close()
        }
        pathLT.apply {
            reset()
            moveTo(0F, 0F)
            lineTo(0F, bound.centerY())
            arcTo(bound, 180F, 90F)
            close()
        }
        pathRT.apply {
            reset()
            moveTo(bound.right, 0F)
            lineTo(bound.centerX(), 0F)
            arcTo(bound, 270F, 90F)
            close()
        }
    }
}