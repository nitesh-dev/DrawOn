package com.flaxstudio.drawon.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.flaxstudio.drawon.utils.Vector2
import java.util.Vector

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var whiteBoardPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }


    private var canvasPosition = Vector2()
    private val whiteBoardRect = Rect(100, 100, 300, 300)
    override fun onDraw(canvas: Canvas) {

//        canvas.scale(2f, 2f)
        canvas.translate(canvasPosition.x, canvasPosition.y)
        canvas.scale(2f, 2f)
        canvas.drawRect(whiteBoardRect, whiteBoardPaint)

    }



    private var previousTouch = Vector2()

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val posX = event.x
        val posY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                Log.e("======", "down")
//                previousTouch.x = posX
//                previousTouch.y = posY
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                // this callback is only for zoom and pan
                if(event.pointerCount > 1){
                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    previousTouch = touch1.minPointVector(touch2)

                }
            }

            MotionEvent.ACTION_MOVE -> {

                // this callback is only for zoom and pan
                if(event.pointerCount > 1){

                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    val currentTouch = touch1.minPointVector(touch2)
                    val deltaChange = currentTouch - previousTouch
                    canvasPosition += deltaChange
                    previousTouch = currentTouch

                    postInvalidate()
                    return true
                }

                // this is for drawing shapes and more
//                val dx = posX - previousTouch.x
//                val dy = posY - previousTouch.y
//                canvasPosition.x += dx
//                canvasPosition.y += dy
//                previousTouch.x = posX
//                previousTouch.y = posY
            }

            MotionEvent.ACTION_UP -> {


            }

            else -> return false

        }

        postInvalidate()
        return true
    }


    private fun vectorMidPoint(vector1: Vector2, vector2: Vector2): Vector2{
        // calculating vector + vectors mid point
        return Vector2(vector1.x - (vector1.x - vector2.x)/2, vector1.y - (vector1.y - vector2.y)/2)
    }
}

