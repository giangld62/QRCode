package com.tapbi.spark.qrcode.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.tapbi.spark.qrcode.R

class ViewfinderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    var scannerLineStart = 0
    var scannerLineEnd = 0
    private var isTopToBottom = true
    private var paint: Paint? = null
    private var backgroundFrameScannerColor = 0
    private var frameScannerColor = 0
    private var frameWidth = 0
    private var frameHeight = 0
    private var frame: Rect? = null
    private var scannerLineMoveDistance = 0
    private var frameLineWidth = 0
    private var scannerAnimationDelay = 0
    private var frameRatio = 0f
    private var framePaddingLeft = 0f
    private var framePaddingTop = 0f
    private var paintLaser: Paint? = null
    private var pathLaser: Path? = null
    private var paintCorner: Paint? = null
    private var pathCorner: Path? = null
    private var isDraw = true
    fun setDraw(draw: Boolean) {
        isDraw = draw
        invalidate()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewfinderView)
        backgroundFrameScannerColor = array.getColor(
            R.styleable.ViewfinderView_maskColor,
            ContextCompat.getColor(context, R.color.viewfinder_mask)
        )
        frameScannerColor = array.getColor(
            R.styleable.ViewfinderView_frameColor,
            ContextCompat.getColor(context, R.color.viewfinder_frame)
        )
        val cornerColor = array.getColor(
            R.styleable.ViewfinderView_cornerColor,
            ContextCompat.getColor(context, R.color.viewfinder_corner)
        )
        val laserLineColor = array.getColor(
            R.styleable.ViewfinderView_laserColor,
            ContextCompat.getColor(context, R.color.viewfinder_laser)
        )
        frameWidth = array.getDimensionPixelSize(R.styleable.ViewfinderView_frameWidth, 0)
        frameHeight = array.getDimensionPixelSize(R.styleable.ViewfinderView_frameHeight, 0)
        val cornerRectWidth = array.getDimension(
            R.styleable.ViewfinderView_cornerRectWidth,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
        ).toInt()
        scannerLineMoveDistance = array.getDimension(
            R.styleable.ViewfinderView_scannerLineMoveDistance,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
        ).toInt()
        val scannerLineHeight = array.getDimension(
            R.styleable.ViewfinderView_scannerLineHeight,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics)
        ).toInt()
        frameLineWidth = array.getDimension(
            R.styleable.ViewfinderView_frameLineWidth,
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics)
        ).toInt()
        scannerAnimationDelay =
            array.getInteger(R.styleable.ViewfinderView_scannerAnimationDelay, 5)
        frameRatio = array.getFloat(R.styleable.ViewfinderView_frameRatio, 0.8f)
        framePaddingLeft = array.getDimension(R.styleable.ViewfinderView_framePaddingLeft, 0f)
        framePaddingTop = array.getDimension(R.styleable.ViewfinderView_framePaddingTop, 0f)
        array.recycle()


        //paint background, frame
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.strokeWidth = 20f

        // Paint and Path Laser Line
        paintLaser = Paint(Paint.ANTI_ALIAS_FLAG)
        paintLaser!!.color = laserLineColor
        paintLaser!!.style = Paint.Style.STROKE
        paintLaser!!.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f,
            getContext().resources.displayMetrics
        ).toInt().toFloat()
        pathLaser = Path()

        //Paint and Path Corner
        paintCorner = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCorner!!.color = cornerColor
        paintCorner!!.strokeWidth = cornerRectWidth.toFloat()
        paintCorner!!.style = Paint.Style.STROKE
        pathCorner = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initFrame(w, h)
    }

    private fun initFrame(width: Int, height: Int) {
        val size = (Math.min(width, height) * frameRatio).toInt()
        if (frameWidth <= 0 || frameWidth > width) {
            frameWidth = size
        }
        if (frameHeight <= 0 || frameHeight > height) {
            frameHeight = size
        }
        val leftOffsets = (width - frameWidth) / 2f + framePaddingLeft
        val topOffsets = (height - frameHeight) / 2f + framePaddingTop
        frame = Rect(
            leftOffsets.toInt(),
            topOffsets.toInt(),
            leftOffsets.toInt() + frameWidth,
            topOffsets.toInt() + frameHeight
        )
    }

    public override fun onDraw(canvas: Canvas) {
        if (isDraw) {
            if (frame == null) {
                return
            }
            if (scannerLineStart == 0 || scannerLineEnd == 0) {
                scannerLineStart = frame!!.top
                scannerLineEnd = frame!!.bottom
            }
            val width = width
            val height = height
            drawBackgroundFrameColor(canvas, frame!!, width, height)
            drawLineScanner(canvas, frame!!)
            drawFrame(canvas, frame!!)
            drawCorner(canvas, frame!!)
            postInvalidateDelayed(
                scannerAnimationDelay.toLong(),
                frame!!.left,
                frame!!.top,
                frame!!.right,
                frame!!.bottom
            )
        } else {
            scannerLineStart = 0
            scannerLineEnd = 0
        }
    }

    private fun drawCorner(canvas: Canvas, frame: Rect) {
        pathCorner!!.reset()
        val padding = 40
        val withCorner = 80
        val radius = 16

        //draw top left
        pathCorner!!.moveTo(
            (frame.left + padding + withCorner).toFloat(),
            (frame.top + padding).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.left + padding + radius).toFloat(),
            (frame.top + padding).toFloat()
        )
        pathCorner!!.cubicTo(
            (frame.left + padding).toFloat(), (frame.top + padding).toFloat(), (
                    frame.left + padding).toFloat(), (frame.top + padding).toFloat(), (
                    frame.left + padding).toFloat(), (frame.top + padding + radius).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.left + padding).toFloat(),
            (frame.top + padding + withCorner).toFloat()
        )
        canvas.drawPath(pathCorner!!, paintCorner!!)

        //top right
        pathCorner!!.moveTo(
            (frame.right - padding - withCorner).toFloat(),
            (frame.top + padding).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.right - padding - radius).toFloat(),
            (frame.top + padding).toFloat()
        )
        pathCorner!!.cubicTo(
            (frame.right - padding).toFloat(), (frame.top + padding).toFloat(), (
                    frame.right - padding).toFloat(), (frame.top + padding).toFloat(), (
                    frame.right - padding).toFloat(), (frame.top + padding + radius).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.right - padding).toFloat(),
            (frame.top + padding + withCorner).toFloat()
        )
        canvas.drawPath(pathCorner!!, paintCorner!!)

        //bottom right
        pathCorner!!.moveTo(
            (frame.left + padding + withCorner).toFloat(),
            (frame.bottom - padding).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.left + padding + radius).toFloat(),
            (frame.bottom - padding).toFloat()
        )
        pathCorner!!.cubicTo(
            (frame.left + padding).toFloat(), (frame.bottom - padding).toFloat(), (
                    frame.left + padding).toFloat(), (frame.bottom - padding).toFloat(), (
                    frame.left + padding).toFloat(), (frame.bottom - padding - radius).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.left + padding).toFloat(),
            (frame.bottom - padding - withCorner).toFloat()
        )
        canvas.drawPath(pathCorner!!, paintCorner!!)


        //bottom left
        pathCorner!!.moveTo(
            (frame.right - padding - withCorner).toFloat(),
            (frame.bottom - padding).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.right - padding - radius).toFloat(),
            (frame.bottom - padding).toFloat()
        )
        pathCorner!!.cubicTo(
            (frame.right - padding).toFloat(), (frame.bottom - padding).toFloat(), (
                    frame.right - padding).toFloat(), (frame.bottom - padding).toFloat(), (
                    frame.right - padding).toFloat(), (frame.bottom - padding - radius).toFloat()
        )
        pathCorner!!.lineTo(
            (frame.right - padding).toFloat(),
            (frame.bottom - padding - withCorner).toFloat()
        )
        canvas.drawPath(pathCorner!!, paintCorner!!)
    }

    private fun drawLineScanner(canvas: Canvas, frame: Rect) {
        pathLaser!!.reset()
        if (isTopToBottom) {
            pathLaser!!.moveTo(frame.right.toFloat(), scannerLineStart.toFloat())
            pathLaser!!.lineTo(frame.left.toFloat(), scannerLineStart.toFloat())
            canvas.drawPath(pathLaser!!, paintLaser!!)
            scannerLineStart += scannerLineMoveDistance
            if (scannerLineStart >= frame.bottom - scannerLineMoveDistance * 3) {
                scannerLineStart = frame.top
                isTopToBottom = false
            }
        } else {
            if (scannerLineEnd > frame.bottom) scannerLineEnd =
                frame.bottom - scannerLineMoveDistance * 3
            pathLaser!!.moveTo(frame.right.toFloat(), scannerLineEnd.toFloat())
            pathLaser!!.lineTo(frame.left.toFloat(), scannerLineEnd.toFloat())
            canvas.drawPath(pathLaser!!, paintLaser!!)
            scannerLineEnd -= scannerLineMoveDistance
            if (scannerLineEnd <= frame.top + scannerLineMoveDistance * 3) {
                scannerLineEnd = frame.bottom
                isTopToBottom = true
            }
        }
    }

    private fun drawFrame(canvas: Canvas, frame: Rect) {
        paint!!.color = frameScannerColor
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            (frame.top + frameLineWidth).toFloat(),
            paint!!
        )
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            (frame.left + frameLineWidth).toFloat(),
            frame.bottom.toFloat(),
            paint!!
        )
        canvas.drawRect(
            (frame.right - frameLineWidth).toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            paint!!
        )
        canvas.drawRect(
            frame.left.toFloat(),
            (frame.bottom - frameLineWidth).toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            paint!!
        )
    }

    private fun drawBackgroundFrameColor(canvas: Canvas, frame: Rect, width: Int, height: Int) {
        if (backgroundFrameScannerColor != 0) {
            paint!!.color = backgroundFrameScannerColor
            canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint!!)
            canvas.drawRect(
                0f, frame.top.toFloat(), frame.left.toFloat(), frame.bottom.toFloat(),
                paint!!
            )
            canvas.drawRect(
                frame.right.toFloat(), frame.top.toFloat(), width.toFloat(), frame.bottom.toFloat(),
                paint!!
            )
            canvas.drawRect(
                0f, frame.bottom.toFloat(), width.toFloat(), height.toFloat(),
                paint!!
            )
        }
    }

    init {
        init(context, attrs)
    }
}