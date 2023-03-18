package com.flaxstudio.drawon.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.abhishek.colorpicker.toPx
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.utils.Vector2
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

class CropView (context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var transparentPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val cropperLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(getContext(), R.color.pointer_color)
    }

    private val cropperPointPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(getContext(), R.color.pointer_color)
        isAntiAlias = true
    }


    private val cropperTouchRadius = 20.toPx.toFloat()      // detect touch
    private val cropperPointSize = 8.toPx.toFloat()
    private var selectedPointer = Pointer.NULL              // used to check which pointer is selected

    private var transparentBitmap: Bitmap? = null           // transparent check background
    private val cropBitmapOffset = 8.toPx                   // offset for drawing bitmap
    private var scaledBitmap: Bitmap? = null                // actual bitmap to draw on canvas
    private val bitmapRect = RectF()                        // bitmap rect
    private val cropperRect = RectF()                       // rect of cropper

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(transparentBitmap == null) createTransparentBitmap()
        canvas.drawBitmap(transparentBitmap!!, 0f, 0f, null)

        // need update in drawBitmap()
        if(scaledBitmap != null) canvas.drawBitmap(scaledBitmap!!, bitmapRect.left, bitmapRect.top, null)

        drawCropper(canvas)
    }

    private fun createTransparentBitmap(){
        val gridSize = 6.toPx.toFloat()
        transparentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(transparentBitmap!!)
        val xTimes = ceil(width.toFloat() / gridSize).toInt()
        val yTimes = ceil(height.toFloat() / gridSize).toInt()
        var startX = 0f
        var startY = 0f

        for (y in 0 .. yTimes){
            for (x in 0.. xTimes){
                changeTransparentPaint(x, y)
                startX = x * gridSize
                startY = y * gridSize
                canvas.drawRect(startX, startY, startX + gridSize, startY + gridSize, transparentPaint)
            }
        }
    }

    private fun changeTransparentPaint(x: Int, y: Int){
        val sum = x + y
        if(sum % 2 == 0) transparentPaint.color = Color.WHITE else transparentPaint.color = Color.LTGRAY
    }

    private fun createScaledBitmap(bitmap: Bitmap){

        var bitmapWidth = 0f
        var bitmapHeight = 0f

        // creating scaled bitmap to fit screen
        if(bitmap.width > bitmap.height){
            val ratio = bitmap.height.toFloat() / bitmap.width
            bitmapWidth = width.toFloat() - 2 * cropBitmapOffset
            bitmapHeight = ratio * bitmapWidth

            bitmapRect.left = cropBitmapOffset.toFloat()
            bitmapRect.top = (height - bitmapHeight) / 2 + cropBitmapOffset
        }else{
            val ratio = bitmap.width.toFloat() / bitmap.height
            bitmapHeight = height.toFloat() - 2 * cropBitmapOffset
            bitmapWidth = ratio * bitmapHeight

            bitmapRect.left = (width - bitmapWidth) / 2 + cropBitmapOffset
            bitmapRect.top = cropBitmapOffset.toFloat()
        }

        scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmapWidth.toInt(), bitmapHeight.toInt(), true)
        bitmapRect.right = bitmapRect.left + bitmapWidth
        bitmapRect.bottom = bitmapRect.top + bitmapHeight
        cropperRect.set(bitmapRect.left, bitmapRect.top, bitmapRect.right, bitmapRect.bottom)
    }
    fun loadBitmap(bitmap: Bitmap){
        createScaledBitmap(bitmap)
        postInvalidate()
    }

    fun resetPointerToDefault(){
        cropperRect.set(bitmapRect.left, bitmapRect.top, bitmapRect.right, bitmapRect.bottom)
        invalidate()
    }


    private fun drawCropper(canvas: Canvas){
        // drawing main lines
        cropperLinePaint.strokeWidth = 2.toPx.toFloat()
        canvas.drawLine(cropperRect.left, cropperRect.top, cropperRect.right, cropperRect.top, cropperLinePaint)         // top
        canvas.drawLine(cropperRect.right, cropperRect.top, cropperRect.right, cropperRect.bottom, cropperLinePaint)     // right
        canvas.drawLine(cropperRect.left, cropperRect.bottom, cropperRect.right, cropperRect.bottom, cropperLinePaint)   // bottom
        canvas.drawLine(cropperRect.left, cropperRect.top, cropperRect.left, cropperRect.bottom, cropperLinePaint)       // left

        // drawing sub lines
        cropperLinePaint.strokeWidth = 1.toPx.toFloat()

        // horizontal
        var gridSize = cropperRect.height() / 3
        canvas.drawLine(cropperRect.left, cropperRect.top + gridSize, cropperRect.right, cropperRect.top + gridSize, cropperLinePaint)
        canvas.drawLine(cropperRect.left, cropperRect.bottom - gridSize, cropperRect.right, cropperRect.bottom - gridSize, cropperLinePaint)

        // vertical
        gridSize = cropperRect.width() / 3
        canvas.drawLine(cropperRect.left + gridSize, cropperRect.top, cropperRect.left + gridSize, cropperRect.bottom, cropperLinePaint)
        canvas.drawLine(cropperRect.right - gridSize, cropperRect.top, cropperRect.right - gridSize, cropperRect.bottom, cropperLinePaint)

        // drawing points
        canvas.drawCircle(cropperRect.left, cropperRect.top, cropperPointSize, cropperPointPaint)
        canvas.drawCircle(cropperRect.right, cropperRect.top, cropperPointSize, cropperPointPaint)
        canvas.drawCircle(cropperRect.right, cropperRect.bottom, cropperPointSize, cropperPointPaint)
        canvas.drawCircle(cropperRect.left, cropperRect.bottom, cropperPointSize, cropperPointPaint)
    }


    // touching behaviours
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchPos = Vector2(event.x, event.y)

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                checkPointerTouched(touchPos)               // check if pointer touched
            }

            MotionEvent.ACTION_MOVE -> {
                if(selectedPointer == Pointer.NULL) return false

                when(selectedPointer){
                    Pointer.TOP_LEFT -> {
                        cropperRect.left = touchPos.x
                        cropperRect.top = touchPos.y
                    }
                    Pointer.TOP_RIGHT -> {
                        cropperRect.right = touchPos.x
                        cropperRect.top = touchPos.y
                    }
                    Pointer.BOTTOM_RIGHT -> {
                        cropperRect.right = touchPos.x
                        cropperRect.bottom = touchPos.y
                    }
                    Pointer.BOTTOM_LEFT-> {
                        cropperRect.left = touchPos.x
                        cropperRect.bottom = touchPos.y
                    }
                    else -> {}
                }
            }

            MotionEvent.ACTION_UP -> {
                selectedPointer = Pointer.NULL
            }
        }

        // limiting further movement
        if(cropperRect.left > cropperRect.right) {
            if(selectedPointer == Pointer.BOTTOM_RIGHT || selectedPointer == Pointer.TOP_RIGHT){
                cropperRect.right = cropperRect.left
            }else cropperRect.left = cropperRect.right
        }
        if(cropperRect.top > cropperRect.bottom) {
            if(selectedPointer == Pointer.BOTTOM_RIGHT || selectedPointer == Pointer.BOTTOM_LEFT){
                cropperRect.bottom = cropperRect.top
            }else cropperRect.top = cropperRect.bottom
        }

        // correcting position
        if(cropperRect.left < bitmapRect.left) cropperRect.left = bitmapRect.left
        if(cropperRect.right > bitmapRect.right) cropperRect.right = bitmapRect.right
        if(cropperRect.top < bitmapRect.top) cropperRect.top = bitmapRect.top
        if(cropperRect.bottom > bitmapRect.bottom) cropperRect.bottom = bitmapRect.bottom

        invalidate()
        return true
        //return super.onTouchEvent(event)
    }

    private var distance = 0f
    private fun checkPointerTouched(touch: Vector2){

        // for top-left
        distance = sqrt((touch.x - cropperRect.left).pow(2) + (touch.y - cropperRect.top).pow(2))
        if(distance < cropperTouchRadius){
            selectedPointer = Pointer.TOP_LEFT
            return
        }

        // for top-right
        distance = sqrt((touch.x - cropperRect.right).pow(2) + (touch.y - cropperRect.top).pow(2))
        if(distance < cropperTouchRadius){
            selectedPointer = Pointer.TOP_RIGHT
            return
        }

        // for bottom-right
        distance = sqrt((touch.x - cropperRect.right).pow(2) + (touch.y - cropperRect.bottom).pow(2))
        if(distance < cropperTouchRadius){
            selectedPointer = Pointer.BOTTOM_RIGHT
            return
        }

        // for bottom-left
        distance = sqrt((touch.x - cropperRect.left).pow(2) + (touch.y - cropperRect.bottom).pow(2))
        if(distance < cropperTouchRadius){
            selectedPointer = Pointer.BOTTOM_LEFT
            return
        }
    }
}

enum class Pointer{
    NULL,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}
